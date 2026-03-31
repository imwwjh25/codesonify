package com.codesonify.domain.entity;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 项目级别的分析结果
 *
 * 聚合整个项目的代码复杂度分析结果
 */
@Data
public class ProjectAnalysis {

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 分析时间
     */
    private LocalDateTime analysisTime;

    /**
     * 所有类的指标列表
     */
    private List<ClassMetrics> classes;

    /**
     * 依赖关系图
     */
    private DependencyGraph dependencyGraph;

    /**
     * 统计信息
     */
    private Statistics statistics;

    /**
     * 项目级别统计信息
     */
    @Data
    public static class Statistics {
        /**
         * 总类数
         */
        private int totalClasses;

        /**
         * 总方法数
         */
        private int totalMethods;

        /**
         * 平均圈复杂度
         */
        private double averageCyclomaticComplexity;

        /**
         * 最大圈复杂度
         */
        private int maxCyclomaticComplexity;

        /**
         * 总代码行数
         */
        private int totalLinesOfCode;

        /**
         * 平均耦合度
         */
        private double averageCoupling;

        /**
         * 循环依赖数量
         */
        private int cycleCount;

        /**
         * 复杂度等级分布
         */
        private ComplexityDistribution complexityDistribution;

        /**
         * 复杂度等级分布
         */
        @Data
        public static class ComplexityDistribution {
            private int simple;
            private int moderate;
            private int complex;
            private int veryComplex;
        }
    }
}
