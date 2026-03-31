package com.codesonify.domain.entity;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 类级别复杂度指标
 *
 * 聚合一个类中所有方法的复杂度信息
 */
@Data
@Builder
public class ClassMetrics {

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
     * 总方法数
     */
    private int totalMethods;

    /**
     * 平均复杂度
     */
    private double averageComplexity;

    /**
     * 最大复杂度
     */
    private int maxComplexity;

    /**
     * 代码行数
     */
    private int linesOfCode;

    /**
     * 对象间耦合度（CBO - Coupling Between Objects）
     */
    private int couplingBetweenObjects;

    /**
     * 方法复杂度列表
     */
    private List<ComplexityMetrics> methods;

    /**
     * 依赖的类列表
     */
    private List<String> dependencies;

    /**
     * 字段数量
     */
    private int numberOfFields;

    /**
     * 构造函数数量
     */
    private int numberOfConstructors;

    /**
     * 静态方法数量
     */
    private int numberOfStaticMethods;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;

    /**
     * 获取复杂度等级分布
     *
     * @return 各等级的方法数量
     */
    public ComplexityDistribution getComplexityDistribution() {
        int simple = 0;
        int moderate = 0;
        int complex = 0;
        int veryComplex = 0;

        for (ComplexityMetrics method : methods) {
            switch (method.getLevel()) {
                case SIMPLE -> simple++;
                case MODERATE -> moderate++;
                case COMPLEX -> complex++;
                case VERY_COMPLEX -> veryComplex++;
            }
        }

        return ComplexityDistribution.builder()
                .simple(simple)
                .moderate(moderate)
                .complex(complex)
                .veryComplex(veryComplex)
                .build();
    }

    /**
     * 复杂度分布
     */
    @Data
    @Builder
    public static class ComplexityDistribution {
        private int simple;
        private int moderate;
        private int complex;
        private int veryComplex;
    }
}
