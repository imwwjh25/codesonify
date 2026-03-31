package com.codesonify.interfaces.controller;

import com.codesonify.application.service.CodeAnalysisService;
import com.codesonify.application.service.TechnicalDebtService;
import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.interfaces.dto.TechnicalDebtAnalysisRequest;
import com.codesonify.interfaces.dto.TechnicalDebtAnalysisResponse;
import com.codesonify.interfaces.dto.TechnicalDebtAnalysisResponse.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 技术债务分析控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/technical-debt")
@Tag(name = "技术债务分析", description = "技术债务量化与分析相关接口")
public class TechnicalDebtController {

    @Autowired
    private TechnicalDebtService technicalDebtService;

    @Autowired
    private CodeAnalysisService codeAnalysisService;

    @PostMapping("/analyze")
    @Operation(summary = "分析项目技术债务", description = "分析指定项目的技术债务，生成分数、热力图和重构建议")
    public ResponseEntity<TechnicalDebtAnalysisResponse> analyze(
            @RequestBody TechnicalDebtAnalysisRequest request) {

        log.info("收到技术债务分析请求，项目路径: {}", request.getProjectPath());

        try {
            // 分析代码获取类指标
            List<ClassMetrics> classMetricsList;
            if (request.getClassMetrics() != null && !request.getClassMetrics().isEmpty()) {
                classMetricsList = request.getClassMetrics();
            } else {
                var analysisResult = codeAnalysisService.analyzeProject(request.getProjectPath());
                classMetricsList = analysisResult.getClasses();
            }

            // 分析技术债务
            TechnicalDebtService.TechnicalDebtAnalysisResult result =
                    technicalDebtService.analyzeProject(classMetricsList);

            // 生成热力图
            String heatmapPath = null;
            if (request.getGenerateHeatmap()) {
                String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
                String filename = "technical-debt-heatmap-" + timestamp + ".drawio";
                String outputPath = Paths.get(request.getOutputDir(), filename).toString();

                try {
                    heatmapPath = technicalDebtService.generateHeatmap(result.getScores(), outputPath);
                } catch (IOException e) {
                    log.error("生成热力图失败: {}", e.getMessage());
                }
            }

            // 构建响应
            TechnicalDebtAnalysisResponse response = TechnicalDebtAnalysisResponse.builder()
                    .scores(result.getScores().stream()
                            .map(TechnicalDebtScoreDTO::fromEntity)
                            .collect(Collectors.toList()))
                    .suggestions(result.getSuggestions().stream()
                            .map(RefactoringSuggestionDTO::fromEntity)
                            .collect(Collectors.toList()))
                    .statistics(TechnicalDebtStatisticsDTO.builder()
                            .totalClasses(result.getStatistics().getTotalClasses())
                            .noneDebtCount(result.getStatistics().getNoneDebtCount())
                            .lowDebtCount(result.getStatistics().getLowDebtCount())
                            .mediumDebtCount(result.getStatistics().getMediumDebtCount())
                            .highDebtCount(result.getStatistics().getHighDebtCount())
                            .criticalDebtCount(result.getStatistics().getCriticalDebtCount())
                            .averageScore(result.getStatistics().getAverageScore())
                            .maxScore(result.getStatistics().getMaxScore())
                            .minScore(result.getStatistics().getMinScore())
                            .totalEstimatedHours(result.getStatistics().getTotalEstimatedHours())
                            .build())
                    .heatmapPath(heatmapPath)
                    .analysisTimestamp(result.getAnalysisTimestamp())
                    .build();

            log.info("技术债务分析完成，共 {} 个类", result.getScores().size());
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("技术债务分析失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/analyze/class")
    @Operation(summary = "分析单个类技术债务", description = "分析指定类的技术债务分数")
    public ResponseEntity<TechnicalDebtScoreDTO> analyzeClass(
            @Parameter(description = "类指标信息") @RequestBody ClassMetrics classMetrics) {

        log.info("收到类技术债务分析请求，类名: {}", classMetrics.getClassName());

        try {
            var score = technicalDebtService.analyzeClass(classMetrics);
            return ResponseEntity.ok(TechnicalDebtScoreDTO.fromEntity(score));
        } catch (Exception e) {
            log.error("类技术债务分析失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/statistics")
    @Operation(summary = "获取技术债务统计", description = "获取项目整体的技术债务统计信息")
    public ResponseEntity<TechnicalDebtStatisticsDTO> getStatistics(
            @Parameter(description = "类指标列表") @RequestBody List<ClassMetrics> classMetricsList) {

        log.info("收到技术债务统计请求，共 {} 个类", classMetricsList.size());

        try {
            var result = technicalDebtService.analyzeProject(classMetricsList);
            var statistics = TechnicalDebtStatisticsDTO.builder()
                    .totalClasses(result.getStatistics().getTotalClasses())
                    .noneDebtCount(result.getStatistics().getNoneDebtCount())
                    .lowDebtCount(result.getStatistics().getLowDebtCount())
                    .mediumDebtCount(result.getStatistics().getMediumDebtCount())
                    .highDebtCount(result.getStatistics().getHighDebtCount())
                    .criticalDebtCount(result.getStatistics().getCriticalDebtCount())
                    .averageScore(result.getStatistics().getAverageScore())
                    .maxScore(result.getStatistics().getMaxScore())
                    .minScore(result.getStatistics().getMinScore())
                    .totalEstimatedHours(result.getStatistics().getTotalEstimatedHours())
                    .build();

            return ResponseEntity.ok(statistics);
        } catch (Exception e) {
            log.error("获取技术债务统计失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/heatmap")
    @Operation(summary = "生成技术债务热力图", description = "基于类指标生成技术债务热力图")
    public ResponseEntity<String> generateHeatmap(
            @RequestBody List<ClassMetrics> classMetricsList,
            @Parameter(description = "输出目录") @RequestParam(defaultValue = "./output/technical-debt") String outputDir) {

        log.info("收到热力图生成请求，共 {} 个类", classMetricsList.size());

        try {
            // 分析技术债务
            var result = technicalDebtService.analyzeProject(classMetricsList);

            // 生成热力图
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String filename = "technical-debt-heatmap-" + timestamp + ".drawio";
            String outputPath = Paths.get(outputDir, filename).toString();

            String heatmapPath = technicalDebtService.generateHeatmap(result.getScores(), outputPath);

            return ResponseEntity.ok(heatmapPath);
        } catch (IOException e) {
            log.error("生成热力图失败", e);
            return ResponseEntity.internalServerError().body("生成热力图失败: " + e.getMessage());
        }
    }

    @PostMapping("/suggestions")
    @Operation(summary = "获取重构建议", description = "基于类指标生成重构建议")
    public ResponseEntity<List<RefactoringSuggestionDTO>> getSuggestions(
            @RequestBody List<ClassMetrics> classMetricsList) {

        log.info("收到重构建议请求，共 {} 个类", classMetricsList.size());

        try {
            // 分析技术债务
            var result = technicalDebtService.analyzeProject(classMetricsList);

            // 转换为 DTO
            List<RefactoringSuggestionDTO> suggestions = result.getSuggestions().stream()
                    .map(RefactoringSuggestionDTO::fromEntity)
                    .collect(Collectors.toList());

            return ResponseEntity.ok(suggestions);
        } catch (Exception e) {
            log.error("获取重构建议失败", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}