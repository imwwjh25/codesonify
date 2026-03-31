package com.codesonify.domain.service;

import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 代码异味聚合器
 *
 * 对代码异味进行聚合、统计和评分
 */
@Service
public class CodeSmellAggregator {

    /**
     * 代码异味统计信息
     */
    @Data
    public static class CodeSmellStatistics {
        private int totalCount;
        private int criticalCount;
        private int highCount;
        private int mediumCount;
        private int lowCount;
        private double averageScore;
        private Map<SmellType, Integer> typeDistribution;
        private Map<Severity, Integer> severityDistribution;
        private List<String> topAffectedClasses;
    }

    /**
     * 按类型聚合代码异味
     *
     * @param codeSmells 代码异味列表
     * @return 按类型分组的代码异味
     */
    public Map<SmellType, List<CodeSmell>> aggregateByType(List<CodeSmell> codeSmells) {
        return codeSmells.stream()
                .collect(Collectors.groupingBy(CodeSmell::getType));
    }

    /**
     * 按严重程度聚合代码异味
     *
     * @param codeSmells 代码异味列表
     * @return 按严重程度分组的代码异味
     */
    public Map<Severity, List<CodeSmell>> aggregateBySeverity(List<CodeSmell> codeSmells) {
        return codeSmells.stream()
                .collect(Collectors.groupingBy(CodeSmell::getSeverity));
    }

    /**
     * 按类名聚合代码异味
     *
     * @param codeSmells 代码异味列表
     * @return 按类名分组的代码异味
     */
    public Map<String, List<CodeSmell>> aggregateByClass(List<CodeSmell> codeSmells) {
        return codeSmells.stream()
                .collect(Collectors.groupingBy(CodeSmell::getClassName));
    }

    /**
     * 计算代码异味评分
     *
     * @param codeSmells 代码异味列表
     * @return 综合评分 (0.0 - 100.0, 越高越差)
     */
    public double calculateScore(List<CodeSmell> codeSmells) {
        if (codeSmells.isEmpty()) {
            return 0.0;
        }

        // 基于严重程度加权计算评分
        double weightedSum = codeSmells.stream()
                .mapToDouble(smell -> {
                    double severityWeight = smell.getSeverity().getScore();
                    // 指数惩罚：严重程度越高，评分增加越快
                    return Math.pow(severityWeight, 2);
                })
                .sum();

        // 标准化到 0-100 范围
        // 假设 10 个严重异味 = 100 分
        double normalizedScore = Math.min(weightedSum * 2, 100.0);

        return Math.round(normalizedScore * 100.0) / 100.0;
    }

    /**
     * 计算代码质量评分 (与异味评分相反)
     *
     * @param codeSmells 代码异味列表
     * @return 质量评分 (0.0 - 100.0, 越高越好)
     */
    public double calculateQualityScore(List<CodeSmell> codeSmells) {
        double smellScore = calculateScore(codeSmells);
        return Math.max(0, 100 - smellScore);
    }

    /**
     * 生成代码异味统计信息
     *
     * @param codeSmells 代码异味列表
     * @return 统计信息
     */
    public CodeSmellStatistics generateStatistics(List<CodeSmell> codeSmells) {
        CodeSmellStatistics stats = new CodeSmellStatistics();

        // 总数和各严重程度数量
        stats.setTotalCount(codeSmells.size());
        stats.setCriticalCount(countBySeverity(codeSmells, Severity.CRITICAL));
        stats.setHighCount(countBySeverity(codeSmells, Severity.HIGH));
        stats.setMediumCount(countBySeverity(codeSmells, Severity.MEDIUM));
        stats.setLowCount(countBySeverity(codeSmells, Severity.LOW));

        // 平均评分
        stats.setAverageScore(calculateScore(codeSmells));

        // 类型分布
        stats.setTypeDistribution(calculateTypeDistribution(codeSmells));

        // 严重程度分布
        stats.setSeverityDistribution(calculateSeverityDistribution(codeSmells));

        // 最受影响的类（按异味数量排序，取前10）
        stats.setTopAffectedClasses(getTopAffectedClasses(codeSmells, 10));

        return stats;
    }

    /**
     * 统计指定严重程度的数量
     *
     * @param codeSmells 代码异味列表
     * @param severity 严重程度
     * @return 数量
     */
    private int countBySeverity(List<CodeSmell> codeSmells, Severity severity) {
        return (int) codeSmells.stream()
                .filter(smell -> smell.getSeverity() == severity)
                .count();
    }

    /**
     * 计算类型分布
     *
     * @param codeSmells 代码异味列表
     * @return 类型分布
     */
    private Map<SmellType, Integer> calculateTypeDistribution(List<CodeSmell> codeSmells) {
        Map<SmellType, Integer> distribution = new EnumMap<>(SmellType.class);

        // 初始化所有类型为 0
        for (SmellType type : SmellType.values()) {
            distribution.put(type, 0);
        }

        // 统计
        codeSmells.forEach(smell -> {
            distribution.put(smell.getType(), distribution.get(smell.getType()) + 1);
        });

        return distribution;
    }

    /**
     * 计算严重程度分布
     *
     * @param codeSmells 代码异味列表
     * @return 严重程度分布
     */
    private Map<Severity, Integer> calculateSeverityDistribution(List<CodeSmell> codeSmells) {
        Map<Severity, Integer> distribution = new EnumMap<>(Severity.class);

        // 初始化所有严重程度为 0
        for (Severity severity : Severity.values()) {
            distribution.put(severity, 0);
        }

        // 统计
        codeSmells.forEach(smell -> {
            distribution.put(smell.getSeverity(), distribution.get(smell.getSeverity()) + 1);
        });

        return distribution;
    }

    /**
     * 获取最受影响的类
     *
     * @param codeSmells 代码异味列表
     * @param limit 数量限制
     * @return 类名列表
     */
    private List<String> getTopAffectedClasses(List<CodeSmell> codeSmells, int limit) {
        return codeSmells.stream()
                .collect(Collectors.groupingBy(
                        CodeSmell::getClassName,
                        Collectors.counting()
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 筛选指定类型的代码异味
     *
     * @param codeSmells 代码异味列表
     * @param type 类型
     * @return 筛选后的列表
     */
    public List<CodeSmell> filterByType(List<CodeSmell> codeSmells, SmellType type) {
        return codeSmells.stream()
                .filter(smell -> smell.getType() == type)
                .collect(Collectors.toList());
    }

    /**
     * 筛选指定严重程度的代码异味
     *
     * @param codeSmells 代码异味列表
     * @param severity 严重程度
     * @return 筛选后的列表
     */
    public List<CodeSmell> filterBySeverity(List<CodeSmell> codeSmells, Severity severity) {
        return codeSmells.stream()
                .filter(smell -> smell.getSeverity() == severity)
                .collect(Collectors.toList());
    }

    /**
     * 筛选指定类的代码异味
     *
     * @param codeSmells 代码异味列表
     * @param className 类名
     * @return 筛选后的列表
     */
    public List<CodeSmell> filterByClass(List<CodeSmell> codeSmells, String className) {
        return codeSmells.stream()
                .filter(smell -> smell.getClassName().equals(className))
                .collect(Collectors.toList());
    }

    /**
     * 获取质量等级
     *
     * @param codeSmells 代码异味列表
     * @return 质量等级
     */
    public String getQualityLevel(List<CodeSmell> codeSmells) {
        double qualityScore = calculateQualityScore(codeSmells);

        if (qualityScore >= 90) {
            return "优秀";
        } else if (qualityScore >= 80) {
            return "良好";
        } else if (qualityScore >= 70) {
            return "中等";
        } else if (qualityScore >= 60) {
            return "及格";
        } else {
            return "需要改进";
        }
    }
}