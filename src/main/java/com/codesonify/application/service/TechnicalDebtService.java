package com.codesonify.application.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.RefactoringSuggestion;
import com.codesonify.domain.entity.TechnicalDebtScore;
import com.codesonify.domain.service.TechnicalDebtAnalyzer;
import com.codesonify.domain.service.TechnicalDebtHeatmapExporter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 技术债务分析应用服务
 *
 * 编排技术债务分析和报告生成
 */
@Slf4j
@Service
public class TechnicalDebtService {

    @Autowired
    private TechnicalDebtAnalyzer analyzer;

    @Autowired
    private TechnicalDebtHeatmapExporter heatmapExporter;

    /**
     * 分析项目技术债务
     */
    public TechnicalDebtAnalysisResult analyzeProject(List<ClassMetrics> classMetricsList) {
        log.info("开始项目技术债务分析");

        List<TechnicalDebtScore> scores = analyzer.analyzeProject(classMetricsList);

        // 生成重构建议
        List<RefactoringSuggestion> suggestions = scores.stream()
                .flatMap(score -> {
                    ClassMetrics classMetrics = classMetricsList.stream()
                            .filter(cm -> cm.getClassName().equals(score.getClassName()))
                            .findFirst()
                            .orElse(null);
                    return analyzer.generateSuggestions(score, classMetrics).stream();
                })
                .collect(Collectors.toList());

        // 统计信息
        TechnicalDebtStatistics statistics = calculateStatistics(scores);

        log.info("项目技术债务分析完成");

        return TechnicalDebtAnalysisResult.builder()
                .scores(scores)
                .suggestions(suggestions)
                .statistics(statistics)
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 生成技术债务热力图
     */
    public String generateHeatmap(List<TechnicalDebtScore> scores, String outputPath) throws IOException {
        log.info("生成技术债务热力图到: {}", outputPath);

        String drawioXml = heatmapExporter.exportToDrawio(scores);

        // 确保输出目录存在
        Path path = Paths.get(outputPath);
        Files.createDirectories(path.getParent());

        // 写入文件
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(drawioXml);
        }

        log.info("技术债务热力图生成成功");
        return outputPath;
    }

    /**
     * 获取类技术债务分析
     */
    public TechnicalDebtScore analyzeClass(ClassMetrics classMetrics) {
        return analyzer.analyzeClass(classMetrics);
    }

    /**
     * 获取类重构建议
     */
    public List<RefactoringSuggestion> getSuggestions(TechnicalDebtScore score, ClassMetrics classMetrics) {
        return analyzer.generateSuggestions(score, classMetrics);
    }

    /**
     * 计算统计信息
     */
    private TechnicalDebtStatistics calculateStatistics(List<TechnicalDebtScore> scores) {
        int total = scores.size();

        Map<TechnicalDebtScore.TechnicalDebtLevel, Long> levelCount = scores.stream()
                .collect(Collectors.groupingBy(
                        TechnicalDebtScore::getDebtLevel,
                        Collectors.counting()
                ));

        double avgScore = scores.stream()
                .mapToDouble(TechnicalDebtScore::getTotalScore)
                .average()
                .orElse(0);

        double maxScore = scores.stream()
                .mapToDouble(TechnicalDebtScore::getTotalScore)
                .max()
                .orElse(0);

        double minScore = scores.stream()
                .mapToDouble(TechnicalDebtScore::getTotalScore)
                .min()
                .orElse(0);

        int totalEstimatedHours = scores.stream()
                .mapToInt(TechnicalDebtScore::getEstimatedFixHours)
                .sum();

        return TechnicalDebtStatistics.builder()
                .totalClasses(total)
                .noneDebtCount(levelCount.getOrDefault(TechnicalDebtScore.TechnicalDebtLevel.NONE, 0L).intValue())
                .lowDebtCount(levelCount.getOrDefault(TechnicalDebtScore.TechnicalDebtLevel.LOW, 0L).intValue())
                .mediumDebtCount(levelCount.getOrDefault(TechnicalDebtScore.TechnicalDebtLevel.MEDIUM, 0L).intValue())
                .highDebtCount(levelCount.getOrDefault(TechnicalDebtScore.TechnicalDebtLevel.HIGH, 0L).intValue())
                .criticalDebtCount(levelCount.getOrDefault(TechnicalDebtScore.TechnicalDebtLevel.CRITICAL, 0L).intValue())
                .averageScore(avgScore)
                .maxScore(maxScore)
                .minScore(minScore)
                .totalEstimatedHours(totalEstimatedHours)
                .build();
    }

    /**
     * 技术债务分析结果
     */
    @lombok.Builder
    @lombok.Data
    public static class TechnicalDebtAnalysisResult {
        private List<TechnicalDebtScore> scores;
        private List<RefactoringSuggestion> suggestions;
        private TechnicalDebtStatistics statistics;
        private Long analysisTimestamp;
    }

    /**
     * 技术债务统计信息
     */
    @lombok.Builder
    @lombok.Data
    public static class TechnicalDebtStatistics {
        private int totalClasses;
        private int noneDebtCount;
        private int lowDebtCount;
        private int mediumDebtCount;
        private int highDebtCount;
        private int criticalDebtCount;
        private double averageScore;
        private double maxScore;
        private double minScore;
        private int totalEstimatedHours;
    }
}