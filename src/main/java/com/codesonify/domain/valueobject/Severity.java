package com.codesonify.domain.valueobject;

/**
 * 代码异味严重程度枚举
 */
public enum Severity {
    /**
     * 严重 - 必须立即处理
     */
    CRITICAL("严重", 4),

    /**
     * 高 - 应该尽快处理
     */
    HIGH("高", 3),

    /**
     * 中 - 建议处理
     */
    MEDIUM("中", 2),

    /**
     * 低 - 可以暂缓处理
     */
    LOW("低", 1);

    private final String displayName;
    private final int score;

    Severity(String displayName, int score) {
        this.displayName = displayName;
        this.score = score;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getScore() {
        return score;
    }
}