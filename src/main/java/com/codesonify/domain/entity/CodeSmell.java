package com.codesonify.domain.entity;

import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 代码异味实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSmell {
    /**
     * 代码异味唯一标识
     */
    private UUID id;

    /**
     * 代码异味类型
     */
    private SmellType type;

    /**
     * 严重程度
     */
    private Severity severity;

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名（可选）
     */
    private String methodName;

    /**
     * 行号
     */
    private int lineNumber;

    /**
     * 问题描述
     */
    private String message;

    /**
     * 修复建议
     */
    private String suggestion;

    /**
     * 检测指标值
     */
    private double metricValue;

    /**
     * 阈值
     */
    private double threshold;

    public static CodeSmell create(SmellType type, Severity severity, String className,
                                   String methodName, int lineNumber, String message,
                                   String suggestion, double metricValue, double threshold) {
        return CodeSmell.builder()
                .id(UUID.randomUUID())
                .type(type)
                .severity(severity)
                .className(className)
                .methodName(methodName)
                .lineNumber(lineNumber)
                .message(message)
                .suggestion(suggestion)
                .metricValue(metricValue)
                .threshold(threshold)
                .build();
    }
}