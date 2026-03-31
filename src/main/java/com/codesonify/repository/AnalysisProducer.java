package com.codesonify.repository;

import com.codesonify.interfaces.dto.AnalysisTask;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;

/**
 * Kafka 分析任务生产者
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnalysisProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String ANALYSIS_TOPIC = "code-analysis";

    /**
     * 发送分析任务到 Kafka
     *
     * @param task 分析任务
     */
    public void sendAnalysisTask(AnalysisTask task) {
        log.info("发送分析任务到 Kafka: taskId={}", task.getTaskId());

        CompletableFuture<SendResult<String, Object>> future =
                kafkaTemplate.send(ANALYSIS_TOPIC, task.getTaskId(), task);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("分析任务发送成功：taskId={}, offset={}",
                        task.getTaskId(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("分析任务发送失败：taskId={}", task.getTaskId(), ex);
            }
        });
    }

    /**
     * 发送项目分析任务
     *
     * @param taskId 任务 ID
     * @param projectPath 项目路径
     */
    public void sendProjectAnalysisTask(String taskId, String projectPath) {
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .projectPath(projectPath)
                .taskType(AnalysisTask.TaskType.ANALYZE_PROJECT)
                .timestamp(System.currentTimeMillis())
                .build();
        sendAnalysisTask(task);
    }

    /**
     * 发送文件分析任务
     *
     * @param taskId 任务 ID
     * @param filePath 文件路径
     */
    public void sendFileAnalysisTask(String taskId, String filePath) {
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .projectPath(filePath)
                .taskType(AnalysisTask.TaskType.ANALYZE_FILE)
                .timestamp(System.currentTimeMillis())
                .build();
        sendAnalysisTask(task);
    }

    /**
     * 发送报告生成任务
     *
     * @param taskId 任务 ID
     * @param analysisId 分析 ID
     * @param outputDir 输出目录
     */
    public void sendReportGenerationTask(String taskId, String analysisId, String outputDir) {
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .projectPath(analysisId + "|" + outputDir)
                .taskType(AnalysisTask.TaskType.GENERATE_REPORT)
                .timestamp(System.currentTimeMillis())
                .build();
        sendAnalysisTask(task);
    }

    /**
     * 发送声音化任务
     *
     * @param taskId 任务 ID
     * @param analysisId 分析 ID
     */
    public void sendSonificationTask(String taskId, String analysisId) {
        AnalysisTask task = AnalysisTask.builder()
                .taskId(taskId)
                .projectPath(analysisId)
                .taskType(AnalysisTask.TaskType.SONIFY)
                .timestamp(System.currentTimeMillis())
                .build();
        sendAnalysisTask(task);
    }
}
