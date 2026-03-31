package com.codesonify.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 复杂度等级枚举
 *
 * 将圈复杂度映射到不同的等级，用于可视化和声音化
 */
@Getter
@RequiredArgsConstructor
public enum ComplexityLevel {

    /**
     * 简单 - 绿色标识，低音区音符
     */
    SIMPLE(1, 5, "简单", "\uD83D\uDFE2"),

    /**
     * 中等 - 黄色标识，中音区音符
     */
    MODERATE(6, 10, "中等", "\uD83D\uDFE1"),

    /**
     * 复杂 - 橙色标识，高音区音符
     */
    COMPLEX(11, 20, "复杂", "\uD83D\uDFE0"),

    /**
     * 非常复杂 - 红色标识，不和谐音程
     */
    VERY_COMPLEX(21, Integer.MAX_VALUE, "非常复杂", "\uD83D\uDD34");

    /**
     * 最小复杂度值
     */
    private final int minComplexity;

    /**
     * 最大复杂度值
     */
    private final int maxComplexity;

    /**
     * 描述
     */
    private final String description;

    /**
     * Emoji 标识
     */
    private final String emoji;

    /**
     * 根据圈复杂度值获取复杂度等级
     *
     * @param complexity 圈复杂度值
     * @return 复杂度等级
     */
    public static ComplexityLevel fromComplexity(int complexity) {
        for (ComplexityLevel level : values()) {
            if (complexity >= level.minComplexity && complexity <= level.maxComplexity) {
                return level;
            }
        }
        return VERY_COMPLEX;
    }

    /**
     * 获取音高范围描述
     *
     * @return 音高范围
     */
    public String getPitchRange() {
        return switch (this) {
            case SIMPLE -> "C4-G4 (低音区)";
            case MODERATE -> "A4-E5 (中音区)";
            case COMPLEX -> "F5-C6 (高音区)";
            case VERY_COMPLEX -> "不和谐音程";
        };
    }
}
