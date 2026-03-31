package com.codesonify.domain.valueobject;

/**
 * 代码异味类型枚举
 */
public enum SmellType {
    /**
     * 长方法 - 方法行数过多或嵌套过深
     */
    LONG_METHOD("长方法"),

    /**
     * 大类 - 类过大（行数、方法数、字段数过多）
     */
    LARGE_CLASS("大类"),

    /**
     * 重复代码 - 存在相似的代码片段
     */
    DUPLICATE_CODE("重复代码"),

    /**
     * 长参数列表 - 方法参数过多
     */
    LONG_PARAMETER_LIST("长参数列表"),

    /**
     * 过度耦合 - 类之间的耦合度过高
     */
    HIGH_COUPLING("过度耦合"),

    /**
     * 上帝类 - 承担过多职责的类
     */
    GOD_CLASS("上帝类"),

    /**
     * 特性依恋 - 方法过度使用其他类的数据
     */
    FEATURE_ENVY("特性依恋"),

    /**
     * 数据类 - 仅包含数据访问器的类
     */
    DATA_CLASS("数据类");

    private final String displayName;

    SmellType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}