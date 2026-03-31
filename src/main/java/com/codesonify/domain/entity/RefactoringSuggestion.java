package com.codesonify.domain.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 重构建议
 *
 * 基于技术债务分析生成的具体重构建议
 */
@Data
@Builder
public class RefactoringSuggestion {

    /**
     * 建议类型
     */
    private SuggestionType type;

    /**
     * 优先级
     */
    private Priority priority;

    /**
     * 目标类名
     */
    private String className;

    /**
     * 目标包名
     */
    private String packageName;

    /**
     * 目标方法名（如果适用）
     */
    private String methodName;

    /**
     * 问题描述
     */
    private String description;

    /**
     * 当前指标值
     */
    private double currentValue;

    /**
     * 建议目标值
     */
    private double targetValue;

    /**
     * 预估影响分数
     */
    private double impactScore;

    /**
     * 详细建议内容
     */
    private String suggestion;

    /**
     * 示例代码（可选）
     */
    private String codeExample;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;

    /**
     * 建议类型枚举
     */
    public enum SuggestionType {
        /**
         * 降低圈复杂度
         */
        REDUCE_COMPLEXITY,

        /**
         * 减少耦合度
         */
        REDUCE_COUPLING,

        /**
         * 拆分大类
         */
        SPLIT_LARGE_CLASS,

        /**
         * 提取方法
         */
        EXTRACT_METHOD,

        /**
         * 减少嵌套
         */
        REDUCE_NESTING,

        /**
         * 减少参数
         */
        REDUCE_PARAMETERS,

        /**
         * 减少局部变量
         */
        REDUCE_VARIABLES,

        /**
         * 循环依赖
         */
        CIRCULAR_DEPENDENCY
    }

    /**
     * 优先级枚举
     */
    public enum Priority {
        /**
         * 紧急 - 严重影响代码质量，应立即处理
         */
        CRITICAL,

        /**
         * 高 - 显著影响代码质量，应尽快处理
         */
        HIGH,

        /**
         * 中 - 影响代码质量，建议处理
         */
        MEDIUM,

        /**
         * 低 - 轻微影响，可在方便时处理
         */
        LOW
    }

    /**
     * 获取建议类型的中文名称
     */
    public String getTypeName() {
        return switch (type) {
            case REDUCE_COMPLEXITY -> "降低圈复杂度";
            case REDUCE_COUPLING -> "减少耦合度";
            case SPLIT_LARGE_CLASS -> "拆分大类";
            case EXTRACT_METHOD -> "提取方法";
            case REDUCE_NESTING -> "减少嵌套";
            case REDUCE_PARAMETERS -> "减少参数";
            case REDUCE_VARIABLES -> "减少局部变量";
            case CIRCULAR_DEPENDENCY -> "循环依赖";
        };
    }

    /**
     * 获取优先级的中文名称
     */
    public String getPriorityName() {
        return switch (priority) {
            case CRITICAL -> "紧急";
            case HIGH -> "高";
            case MEDIUM -> "中";
            case LOW -> "低";
        };
    }

    /**
     * 获取优先级的颜色代码
     */
    public String getPriorityColor() {
        return switch (priority) {
            case CRITICAL -> "#D50000";  // 红色
            case HIGH -> "#FF6D00";     // 橙色
            case MEDIUM -> "#FFD600";   // 黄色
            case LOW -> "#64DD17";      // 绿色
        };
    }
}