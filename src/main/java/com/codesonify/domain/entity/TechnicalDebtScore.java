package com.codesonify.domain.entity;

import lombok.Builder;
import lombok.Data;

/**
 * 技术债务分数
 *
 * 基于代码复杂度、耦合度等指标计算的技术债务评分
 */
@Data
@Builder
public class TechnicalDebtScore {

    /**
     * 类名
     */
    private String className;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 总技术债务分数 (0-100)
     * 分数越高，技术债务越严重
     */
    private double totalScore;

    /**
     * 圈复杂度债务分数
     */
    private double complexityScore;

    /**
     * 耦合度债务分数
     */
    private double couplingScore;

    /**
     * 代码行数债务分数
     */
    private double locScore;

    /**
     * 嵌套深度债务分数
     */
    private double nestingScore;

    /**
     * 方法数量债务分数
     */
    private double methodCountScore;

    /**
     * 债务等级
     */
    private TechnicalDebtLevel debtLevel;

    /**
     * 预估修复工时（小时）
     */
    private int estimatedFixHours;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;

    /**
     * 是否存在循环依赖
     */
    private boolean circularDependency;

    /**
     * 技术债务等级枚举
     */
    public enum TechnicalDebtLevel {
        /**
         * 无债务 (0-20)
         */
        NONE,

        /**
         * 低债务 (20-40)
         */
        LOW,

        /**
         * 中等债务 (40-60)
         */
        MEDIUM,

        /**
         * 高债务 (60-80)
         */
        HIGH,

        /**
         * 严重债务 (80-100)
         */
        CRITICAL
    }

    /**
     * 根据总分数确定债务等级
     */
    public static TechnicalDebtLevel determineLevel(double score) {
        if (score < 20) {
            return TechnicalDebtLevel.NONE;
        } else if (score < 40) {
            return TechnicalDebtLevel.LOW;
        } else if (score < 60) {
            return TechnicalDebtLevel.MEDIUM;
        } else if (score < 80) {
            return TechnicalDebtLevel.HIGH;
        } else {
            return TechnicalDebtLevel.CRITICAL;
        }
    }

    /**
     * 获取债务等级的中文名称
     */
    public String getLevelName() {
        return switch (debtLevel) {
            case NONE -> "无债务";
            case LOW -> "低债务";
            case MEDIUM -> "中等债务";
            case HIGH -> "高债务";
            case CRITICAL -> "严重债务";
        };
    }

    /**
     * 获取债务等级的颜色代码（用于热力图）
     */
    public String getLevelColor() {
        return switch (debtLevel) {
            case NONE -> "#00C853";    // 绿色
            case LOW -> "#64DD17";     // 浅绿
            case MEDIUM -> "#FFD600";  // 黄色
            case HIGH -> "#FF6D00";    // 橙色
            case CRITICAL -> "#D50000"; // 红色
        };
    }
}