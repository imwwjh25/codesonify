package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 代码异味检测器
 *
 * 检测常见代码异味，包括长方法、大类、长参数列表、过度耦合等
 */
@Service
public class CodeSmellDetector {

    // 默认阈值配置
    private static final int DEFAULT_LONG_METHOD_MAX_LINES = 50;
    private static final int DEFAULT_LONG_METHOD_MAX_NESTING_DEPTH = 4;
    private static final int DEFAULT_LONG_METHOD_MAX_COMPLEXITY = 10;
    private static final int DEFAULT_LARGE_CLASS_MAX_LINES = 300;
    private static final int DEFAULT_LARGE_CLASS_MAX_METHODS = 15;
    private static final int DEFAULT_LARGE_CLASS_MAX_FIELDS = 20;
    private static final int DEFAULT_LONG_PARAMETER_LIST_MAX_PARAMS = 5;
    private static final int DEFAULT_HIGH_COUPLING_MAX_CBO = 10;
    private static final int DEFAULT_GOD_CLASS_THRESHOLD = 50; // 高耦合 + 大类

    // 可配置阈值
    private int longMethodMaxLines = DEFAULT_LONG_METHOD_MAX_LINES;
    private int longMethodMaxNestingDepth = DEFAULT_LONG_METHOD_MAX_NESTING_DEPTH;
    private int longMethodMaxComplexity = DEFAULT_LONG_METHOD_MAX_COMPLEXITY;
    private int largeClassMaxLines = DEFAULT_LARGE_CLASS_MAX_LINES;
    private int largeClassMaxMethods = DEFAULT_LARGE_CLASS_MAX_METHODS;
    private int largeClassMaxFields = DEFAULT_LARGE_CLASS_MAX_FIELDS;
    private int longParameterListMaxParams = DEFAULT_LONG_PARAMETER_LIST_MAX_PARAMS;
    private int highCouplingMaxCBO = DEFAULT_HIGH_COUPLING_MAX_CBO;

    /**
     * 检测所有代码异味
     *
     * @param classMetricsList 类指标列表
     * @return 检测到的代码异味列表
     */
    public List<CodeSmell> detectCodeSmells(List<ClassMetrics> classMetricsList) {
        List<CodeSmell> codeSmells = new ArrayList<>();

        for (ClassMetrics classMetrics : classMetricsList) {
            codeSmells.addAll(detectClassCodeSmells(classMetrics));
        }

        return codeSmells;
    }

    /**
     * 检测单个类的代码异味
     *
     * @param classMetrics 类指标
     * @return 检测到的代码异味列表
     */
    public List<CodeSmell> detectClassCodeSmells(ClassMetrics classMetrics) {
        List<CodeSmell> codeSmells = new ArrayList<>();

        // 检测大类
        detectLargeClass(classMetrics).ifPresent(codeSmells::add);

        // 检测上帝类
        detectGodClass(classMetrics).ifPresent(codeSmells::add);

        // 检测数据类
        detectDataClass(classMetrics).ifPresent(codeSmells::add);

        // 检测方法级别的异味
        for (ComplexityMetrics methodMetrics : classMetrics.getMethods()) {
            // 检测长方法
            detectLongMethod(methodMetrics).ifPresent(codeSmells::add);

            // 检测长参数列表
            detectLongParameterList(methodMetrics).ifPresent(codeSmells::add);
        }

        // 检测过度耦合
        detectHighCoupling(classMetrics).ifPresent(codeSmells::add);

        return codeSmells;
    }

    /**
     * 检测长方法
     *
     * @param methodMetrics 方法指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectLongMethod(ComplexityMetrics methodMetrics) {
        // 检查行数
        if (methodMetrics.getLinesOfCode() > longMethodMaxLines) {
            Severity severity = calculateSeverity(
                    methodMetrics.getLinesOfCode(),
                    longMethodMaxLines,
                    longMethodMaxLines * 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LONG_METHOD,
                    severity,
                    methodMetrics.getClassName(),
                    methodMetrics.getMethodName(),
                    0,
                    String.format("方法 %s 行数为 %d，超过阈值 %d",
                            methodMetrics.getMethodName(),
                            methodMetrics.getLinesOfCode(),
                            longMethodMaxLines),
                    String.format("建议将方法拆分为多个小方法，每个方法只做一件事"),
                    methodMetrics.getLinesOfCode(),
                    longMethodMaxLines
            ));
        }

        // 检查嵌套深度
        if (methodMetrics.getNestingDepth() > longMethodMaxNestingDepth) {
            Severity severity = calculateSeverity(
                    methodMetrics.getNestingDepth(),
                    longMethodMaxNestingDepth,
                    longMethodMaxNestingDepth + 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LONG_METHOD,
                    severity,
                    methodMetrics.getClassName(),
                    methodMetrics.getMethodName(),
                    0,
                    String.format("方法 %s 嵌套深度为 %d，超过阈值 %d",
                            methodMetrics.getMethodName(),
                            methodMetrics.getNestingDepth(),
                            longMethodMaxNestingDepth),
                    String.format("建议使用卫语句（Guard Clauses）或提取方法来减少嵌套"),
                    methodMetrics.getNestingDepth(),
                    longMethodMaxNestingDepth
            ));
        }

        // 检查圈复杂度
        if (methodMetrics.getCyclomaticComplexity() > longMethodMaxComplexity) {
            Severity severity = calculateSeverity(
                    methodMetrics.getCyclomaticComplexity(),
                    longMethodMaxComplexity,
                    longMethodMaxComplexity + 10
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LONG_METHOD,
                    severity,
                    methodMetrics.getClassName(),
                    methodMetrics.getMethodName(),
                    0,
                    String.format("方法 %s 圈复杂度为 %d，超过阈值 %d",
                            methodMetrics.getMethodName(),
                            methodMetrics.getCyclomaticComplexity(),
                            longMethodMaxComplexity),
                    String.format("建议拆分方法或使用策略模式降低复杂度"),
                    methodMetrics.getCyclomaticComplexity(),
                    longMethodMaxComplexity
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 检测大类
     *
     * @param classMetrics 类指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectLargeClass(ClassMetrics classMetrics) {
        // 检查行数
        if (classMetrics.getLinesOfCode() > largeClassMaxLines) {
            Severity severity = calculateSeverity(
                    classMetrics.getLinesOfCode(),
                    largeClassMaxLines,
                    largeClassMaxLines * 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LARGE_CLASS,
                    severity,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 行数为 %d，超过阈值 %d",
                            classMetrics.getClassName(),
                            classMetrics.getLinesOfCode(),
                            largeClassMaxLines),
                    String.format("建议将大类拆分为多个职责单一的小类"),
                    classMetrics.getLinesOfCode(),
                    largeClassMaxLines
            ));
        }

        // 检查方法数
        if (classMetrics.getTotalMethods() > largeClassMaxMethods) {
            Severity severity = calculateSeverity(
                    classMetrics.getTotalMethods(),
                    largeClassMaxMethods,
                    largeClassMaxMethods * 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LARGE_CLASS,
                    severity,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 方法数为 %d，超过阈值 %d",
                            classMetrics.getClassName(),
                            classMetrics.getTotalMethods(),
                            largeClassMaxMethods),
                    String.format("建议将相关方法提取到单独的类中"),
                    classMetrics.getTotalMethods(),
                    largeClassMaxMethods
            ));
        }

        // 检查字段数
        if (classMetrics.getNumberOfFields() > largeClassMaxFields) {
            Severity severity = calculateSeverity(
                    classMetrics.getNumberOfFields(),
                    largeClassMaxFields,
                    largeClassMaxFields * 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LARGE_CLASS,
                    severity,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 字段数为 %d，超过阈值 %d",
                            classMetrics.getClassName(),
                            classMetrics.getNumberOfFields(),
                            largeClassMaxFields),
                    String.format("建议将相关字段封装到值对象中"),
                    classMetrics.getNumberOfFields(),
                    largeClassMaxFields
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 检测长参数列表
     *
     * @param methodMetrics 方法指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectLongParameterList(ComplexityMetrics methodMetrics) {
        if (methodMetrics.getNumberOfParameters() > longParameterListMaxParams) {
            Severity severity = calculateSeverity(
                    methodMetrics.getNumberOfParameters(),
                    longParameterListMaxParams,
                    longParameterListMaxParams + 5
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.LONG_PARAMETER_LIST,
                    severity,
                    methodMetrics.getClassName(),
                    methodMetrics.getMethodName(),
                    0,
                    String.format("方法 %s 参数个数为 %d，超过阈值 %d",
                            methodMetrics.getMethodName(),
                            methodMetrics.getNumberOfParameters(),
                            longParameterListMaxParams),
                    String.format("建议使用参数对象或 Builder 模式减少参数数量"),
                    methodMetrics.getNumberOfParameters(),
                    longParameterListMaxParams
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 检测过度耦合
     *
     * @param classMetrics 类指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectHighCoupling(ClassMetrics classMetrics) {
        if (classMetrics.getCouplingBetweenObjects() > highCouplingMaxCBO) {
            Severity severity = calculateSeverity(
                    classMetrics.getCouplingBetweenObjects(),
                    highCouplingMaxCBO,
                    highCouplingMaxCBO * 2
            );

            return java.util.Optional.of(CodeSmell.create(
                    SmellType.HIGH_COUPLING,
                    severity,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 的耦合度为 %d，超过阈值 %d",
                            classMetrics.getClassName(),
                            classMetrics.getCouplingBetweenObjects(),
                            highCouplingMaxCBO),
                    String.format("建议使用依赖注入、接口隔离或重构来降低耦合度"),
                    classMetrics.getCouplingBetweenObjects(),
                    highCouplingMaxCBO
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 检测上帝类
     *
     * @param classMetrics 类指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectGodClass(ClassMetrics classMetrics) {
        // 上帝类通常是大类且高耦合
        boolean isLargeClass = classMetrics.getLinesOfCode() > largeClassMaxLines
                || classMetrics.getTotalMethods() > largeClassMaxMethods;
        boolean isHighCoupled = classMetrics.getCouplingBetweenObjects() > highCouplingMaxCBO;

        if (isLargeClass && isHighCoupled) {
            return java.util.Optional.of(CodeSmell.create(
                    SmellType.GOD_CLASS,
                    Severity.CRITICAL,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 是上帝类，承担过多职责且高耦合",
                            classMetrics.getClassName()),
                    String.format("建议严格遵循单一职责原则，将类拆分为多个职责单一的类"),
                    classMetrics.getLinesOfCode() + classMetrics.getCouplingBetweenObjects(),
                    DEFAULT_GOD_CLASS_THRESHOLD
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 检测数据类
     *
     * @param classMetrics 类指标
     * @return 代码异味（如果检测到）
     */
    public java.util.Optional<CodeSmell> detectDataClass(ClassMetrics classMetrics) {
        // 数据类通常是只有 getter/setter 的类，没有业务逻辑
        boolean hasNonAccessorMethods = classMetrics.getMethods().stream()
                .anyMatch(m -> !isAccessorMethod(m.getMethodName()));

        // 如果没有非访问器方法，且有一定数量的字段，则可能是数据类
        if (!hasNonAccessorMethods && classMetrics.getNumberOfFields() > 3) {
            return java.util.Optional.of(CodeSmell.create(
                    SmellType.DATA_CLASS,
                    Severity.MEDIUM,
                    classMetrics.getClassName(),
                    null,
                    0,
                    String.format("类 %s 可能是数据类，仅包含数据访问器",
                            classMetrics.getClassName()),
                    String.format("建议将业务逻辑移到数据类中，或使用不可变对象"),
                    0,
                    0
            ));
        }

        return java.util.Optional.empty();
    }

    /**
     * 判断是否为访问器方法
     *
     * @param methodName 方法名
     * @return 是否为访问器方法
     */
    private boolean isAccessorMethod(String methodName) {
        return methodName.startsWith("get")
                || methodName.startsWith("set")
                || methodName.startsWith("is")
                || methodName.equals("equals")
                || methodName.equals("hashCode")
                || methodName.equals("toString");
    }

    /**
     * 计算严重程度
     *
     * @param value 当前值
     * @param threshold 阈值
     * @param criticalThreshold 严重阈值
     * @return 严重程度
     */
    private Severity calculateSeverity(double value, double threshold, double criticalThreshold) {
        if (value >= criticalThreshold) {
            return Severity.CRITICAL;
        } else if (value >= threshold * 1.5) {
            return Severity.HIGH;
        } else if (value >= threshold * 1.2) {
            return Severity.MEDIUM;
        } else {
            return Severity.LOW;
        }
    }

    // Getter 和 Setter 方法
    public int getLongMethodMaxLines() {
        return longMethodMaxLines;
    }

    public void setLongMethodMaxLines(int longMethodMaxLines) {
        this.longMethodMaxLines = longMethodMaxLines;
    }

    public int getLongMethodMaxNestingDepth() {
        return longMethodMaxNestingDepth;
    }

    public void setLongMethodMaxNestingDepth(int longMethodMaxNestingDepth) {
        this.longMethodMaxNestingDepth = longMethodMaxNestingDepth;
    }

    public int getLongMethodMaxComplexity() {
        return longMethodMaxComplexity;
    }

    public void setLongMethodMaxComplexity(int longMethodMaxComplexity) {
        this.longMethodMaxComplexity = longMethodMaxComplexity;
    }

    public int getLargeClassMaxLines() {
        return largeClassMaxLines;
    }

    public void setLargeClassMaxLines(int largeClassMaxLines) {
        this.largeClassMaxLines = largeClassMaxLines;
    }

    public int getLargeClassMaxMethods() {
        return largeClassMaxMethods;
    }

    public void setLargeClassMaxMethods(int largeClassMaxMethods) {
        this.largeClassMaxMethods = largeClassMaxMethods;
    }

    public int getLargeClassMaxFields() {
        return largeClassMaxFields;
    }

    public void setLargeClassMaxFields(int largeClassMaxFields) {
        this.largeClassMaxFields = largeClassMaxFields;
    }

    public int getLongParameterListMaxParams() {
        return longParameterListMaxParams;
    }

    public void setLongParameterListMaxParams(int longParameterListMaxParams) {
        this.longParameterListMaxParams = longParameterListMaxParams;
    }

    public int getHighCouplingMaxCBO() {
        return highCouplingMaxCBO;
    }

    public void setHighCouplingMaxCBO(int highCouplingMaxCBO) {
        this.highCouplingMaxCBO = highCouplingMaxCBO;
    }
}