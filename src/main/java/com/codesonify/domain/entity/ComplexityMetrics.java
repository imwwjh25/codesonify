package com.codesonify.domain.entity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 复杂度分析结果
 *
 * 存储代码方法的各项复杂度指标
 */
@Data
@Builder
public class ComplexityMetrics {

    /**
     * 类名
     */
    private String className;

    /**
     * 方法名
     */
    private String methodName;

    /**
     * 方法签名
     */
    private String signature;

    /**
     * 圈复杂度（Cyclomatic Complexity）
     * 公式：CC = E - N + 2P 或简化为 CC = 1 + 决策点数量
     */
    private int cyclomaticComplexity;

    /**
     * 代码行数（Lines of Code）
     */
    private int linesOfCode;

    /**
     * 嵌套深度（Nesting Depth）
     */
    private int nestingDepth;

    /**
     * 参数个数
     */
    private int numberOfParameters;

    /**
     * 局部变量个数
     */
    private int numberOfLocalVariables;

    /**
     * 方法类型
     */
    private MethodType methodType;

    /**
     * 依赖的类列表
     */
    private List<String> dependencies;

    /**
     * 复杂度等级
     */
    private ComplexityLevel level;

    /**
     * 所在包名
     */
    private String packageName;

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;
}
