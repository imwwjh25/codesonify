package com.codesonify.interface.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 代码分析响应 DTO
 */
@Data
@Builder
public class AnalysisResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 项目分析结果
     */
    private ProjectAnalysisDTO projectAnalysis;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 项目分析结果 DTO
     */
    @Data
    @Builder
    public static class ProjectAnalysisDTO {
        private String projectName;
        private String projectPath;
        private String analysisTime;
        private List<ClassMetricsDTO> classes;
        private StatisticsDTO statistics;
    }

    /**
     * 类指标 DTO
     */
    @Data
    @Builder
    public static class ClassMetricsDTO {
        private String className;
        private String packageName;
        private int totalMethods;
        private double averageComplexity;
        private int maxComplexity;
        private int linesOfCode;
        private List<MethodMetricsDTO> methods;
    }

    /**
     * 方法指标 DTO
     */
    @Data
    @Builder
    public static class MethodMetricsDTO {
        private String methodName;
        private String signature;
        private int cyclomaticComplexity;
        private int linesOfCode;
        private int nestingDepth;
        private String complexityLevel;
        private String methodType;
    }

    /**
     * 统计信息 DTO
     */
    @Data
    @Builder
    public static class StatisticsDTO {
        private int totalClasses;
        private int totalMethods;
        private double averageCyclomaticComplexity;
        private int maxCyclomaticComplexity;
        private int totalLinesOfCode;
        private ComplexityDistributionDTO complexityDistribution;
    }

    /**
     * 复杂度分布 DTO
     */
    @Data
    @Builder
    public static class ComplexityDistributionDTO {
        private int simple;      // 简单 (1-5)
        private int moderate;    // 中等 (6-10)
        private int complex;     // 复杂 (11-20)
        private int veryComplex; // 非常复杂 (>20)
    }
}
