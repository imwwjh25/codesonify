package com.codesonify.interfaces.dto;

import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码异味 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSmellDTO {
    /**
     * 代码异味 ID
     */
    private String id;

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
}