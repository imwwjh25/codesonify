package com.codesonify.repository;

import com.codesonify.application.service.CodeAnalysisService;
import com.codesonify.application.service.CodeSonificationService;
import com.codesonify.application.service.ReportGenerationService;
import com.codesonify.domain.entity.AnalysisTaskStatus;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.interfaces.dto.AnalysisTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Kafka 分析任务消费者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisConsumer {

    private final CodeAnalysisService codeAnalysisService;
    private final CodeSonificationService codeSonificationService;
    private final ReportGenerationService reportGenerationService;
    private final CacheService cacheService;

    /**
     * 任务状态存储（内存存储，生产环境应使用数据库）
     */
    private final ConcurrentHashMap<String, AnalysisTaskStatus> taskStatusMap = new ConcurrentHashMap<>();

    /**
     * 监听分析任务
     */
    @KafkaListener(topics = "code-analysis", groupId = "analysis-group")
    public void consumeAnalysisTask(AnalysisTask task, Acknowledgment ack) {
        log.info("接收到分析任务：taskId={}, type={}", task.getTaskId(), task.getTaskType());

        try {
            // 更新状态为处理中
            updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.PROCESSING, null, null, null);

            // 根据任务类型处理
            switch (task.getTaskType()) {
                case ANALYZE_PROJECT -> handleProjectAnalysis(task);
                case ANALYZE_FILE -> handleFileAnalysis(task);
                case GENERATE_REPORT -> handleReportGeneration(task);
                case SONIFY -> handleSonification(task);
            }

            // 手动确认
            ack.acknowledge();
            log.info("分析任务完成：taskId={}", task.getTaskId());

        } catch (Exception e) {
            log.error("分析任务失败：taskId={}", task.getTaskId(), e);
            updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.FAILED, null, null, e.getMessage());
            // 失败时也确认消息，避免无限重试
            ack.acknowledge();
        }
    }

    /**
     * 处理项目分析任务
     */
    private void handleProjectAnalysis(AnalysisTask task) throws Exception {
        log.info("开始项目分析：{}", task.getProjectPath());

        // 执行分析
        ProjectAnalysis analysis = codeAnalysisService.analyzeProject(task.getProjectPath());

        // 生成分析 ID
        String analysisId = task.getTaskId();

        // 缓存结果
        cacheService.cacheAnalysisResult(analysisId, analysis);

        // 更新状态
        updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.COMPLETED, analysisId, null, null);

        log.info("项目分析完成：analysisId={}", analysisId);
    }

    /**
     * 处理文件分析任务
     */
    private void handleFileAnalysis(AnalysisTask task) throws Exception {
        log.info("开始文件分析：{}", task.getProjectPath());

        // 执行分析
        var classMetrics = codeAnalysisService.analyzeFile(task.getProjectPath());

        // 生成分析 ID
        String analysisId = task.getTaskId();

        // 更新状态
        updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.COMPLETED, analysisId, null, null);

        log.info("文件分析完成：analysisId={}", analysisId);
    }

    /**
     * 处理报告生成任务
     */
    private void handleReportGeneration(AnalysisTask task) throws Exception {
        String[] parts = task.getProjectPath().split("\\|");
        String analysisId = parts[0];
        String outputDir = parts[1];

        log.info("开始生成报告：analysisId={}, outputDir={}", analysisId, outputDir);

        // 从缓存获取分析结果
        ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
        if (analysis == null) {
            throw new IllegalStateException("分析结果不存在：" + analysisId);
        }

        // 生成报告
        reportGenerationService.generateFullReport(analysis, outputDir, null);

        // 更新状态
        updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.COMPLETED, null, null, null);

        log.info("报告生成完成：{}", outputDir);
    }

    /**
     * 处理声音化任务
     */
    private void handleSonification(AnalysisTask task) throws Exception {
        String analysisId = task.getProjectPath();

        log.info("开始声音化：analysisId={}", analysisId);

        // 从缓存获取分析结果
        ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
        if (analysis == null) {
            throw new IllegalStateException("分析结果不存在：" + analysisId);
        }

        // TODO: 实现声音化逻辑

        // 更新状态
        updateTaskStatus(task.getTaskId(), AnalysisTaskStatus.TaskStatus.COMPLETED, null, null, null);

        log.info("声音化完成：analysisId={}", analysisId);
    }

    /**
     * 更新任务状态
     */
    private void updateTaskStatus(String taskId, AnalysisTaskStatus.TaskStatus status,
                                   String analysisId, String projectPath, String errorMessage) {
        AnalysisTaskStatus taskStatus = taskStatusMap.computeIfAbsent(taskId, k -> {
            AnalysisTaskStatus s = new AnalysisTaskStatus();
            s.setTaskId(k);
            s.setCreatedAt(LocalDateTime.now());
            return s;
        });

        taskStatus.setStatus(status);
        if (analysisId != null) {
            taskStatus.setAnalysisId(analysisId);
        }
        if (projectPath != null) {
            taskStatus.setProjectPath(projectPath);
        }
        taskStatus.setErrorMessage(errorMessage);

        if (status == AnalysisTaskStatus.TaskStatus.COMPLETED ||
            status == AnalysisTaskStatus.TaskStatus.FAILED) {
            taskStatus.setCompletedAt(LocalDateTime.now());
        }
    }

    /**
     * 获取任务状态
     */
    public AnalysisTaskStatus getTaskStatus(String taskId) {
        return taskStatusMap.get(taskId);
    }
}
