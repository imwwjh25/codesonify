package com.codesonify.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 方法类型枚举
 *
 * 用于将代码方法分类，并映射到不同的音色
 */
@Getter
@RequiredArgsConstructor
public enum MethodType {

    /**
     * 构造函数 - 钢琴音色
     */
    CONSTRUCTOR("构造函数", "piano", 0),

    /**
     * Getter 方法 - 吉他音色
     */
    GETTER("Getter", "guitar", 24),

    /**
     * Setter 方法 - 吉他音色
     */
    SETTER("Setter", "guitar", 24),

    /**
     * 业务逻辑方法 - 小提琴音色
     */
    BUSINESS_LOGIC("业务逻辑", "violin", 40),

    /**
     * 工具方法 - 长笛音色
     */
    UTILITY("工具方法", "flute", 73),

    /**
     * 测试方法 - 合成鼓音色
     */
    TEST("测试方法", "drum", 118);

    /**
     * 方法类型描述
     */
    private final String description;

    /**
     * 对应的乐器名称
     */
    private final String instrument;

    /**
     * MIDI 乐器编号（General MIDI）
     */
    private final int midiInstrument;

    /**
     * 根据方法名判断方法类型
     *
     * @param methodName 方法名
     * @return 方法类型
     */
    public static MethodType fromMethodName(String methodName) {
        if (methodName == null || methodName.isEmpty()) {
            return UTILITY;
        }

        // 构造函数
        if (methodName.equals("<init>") || methodName.equals("<clinit>")) {
            return CONSTRUCTOR;
        }

        // Getter
        if (methodName.startsWith("get") || methodName.startsWith("is") || methodName.startsWith("has")) {
            return GETTER;
        }

        // Setter
        if (methodName.startsWith("set")) {
            return SETTER;
        }

        // 测试方法
        if (methodName.contains("Test") || methodName.startsWith("test") ||
            methodName.contains("Should") || methodName.startsWith("should")) {
            return TEST;
        }

        // 工具方法
        if (methodName.startsWith("util") || methodName.startsWith("helper") ||
            methodName.contains("Util") || methodName.contains("Helper")) {
            return UTILITY;
        }

        // 默认为业务逻辑
        return BUSINESS_LOGIC;
    }
}
