package com.codesonify.domain.service;

import com.codesonify.domain.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 技术债务分析器
 *
 * 基于代码复杂度指标计算技术债务分数并生成重构建议
 */
@Slf4j
@Service
public class TechnicalDebtAnalyzer {

    /**
     * 圈复杂度阈值配置
     */
    private static final int CC_SIMPLE = 5;
    private static final int CC_MODERATE = 10;
    private static final int CC_COMPLEX = 20;

    /**
     * 类代码行数阈值
     */
    private static final int LOC_SMALL = 100;
    private static final int LOC_MEDIUM = 300;
    private static final int LOC_LARGE = 500;

    /**
     * 耦合度阈值
     */
    private static final int CBO_LOW = 5;
    private static final int CBO_MEDIUM = 10;
    private static final int CBO_HIGH = 20;

    /**
     * 方法数量阈值
     */
    private static final int METHODS_FEW = 10;
    private static final int METHODS_MANY = 20;
    private static final int METHODS_TOO_MANY = 30;

    /**
     * 嵌套深度阈值
     */
    private static final int NESTING_SHALLOW = 2;
    private static final int NESTING_DEEP = 4;
    private static final int NESTING_VERY_DEEP = 6;

    /**
     * 参数数量阈值
     */
    private static final int PARAMS_FEW = 3;
    private static final int PARAMS_MANY = 5;
    private static final int PARAMS_TOO_MANY = 7;

    /**
     * 局部变量数量阈值
     */
    private static final int VARIABLES_FEW = 5;
    private static final int VARIABLES_MANY = 8;
    private static final int VARIABLES_TOO_MANY = 10;

    /**
     * 分析单个类的技术债务
     */
    public TechnicalDebtScore analyzeClass(ClassMetrics classMetrics) {
        double complexityScore = calculateComplexityScore(classMetrics);
        double couplingScore = calculateCouplingScore(classMetrics);
        double locScore = calculateLocScore(classMetrics);
        double nestingScore = calculateNestingScore(classMetrics);
        double methodCountScore = calculateMethodCountScore(classMetrics);

        // 加权总分（可调整权重）
        double totalScore = (complexityScore * 0.3)
                + (couplingScore * 0.2)
                + (locScore * 0.2)
                + (nestingScore * 0.15)
                + (methodCountScore * 0.15);

        // 计算预估修复工时（基于分数）
        int estimatedFixHours = calculateEstimatedFixHours(totalScore);

        TechnicalDebtScore.TechnicalDebtLevel debtLevel = TechnicalDebtScore.determineLevel(totalScore);

        return TechnicalDebtScore.builder()
                .className(classMetrics.getClassName())
                .packageName(classMetrics.getPackageName())
                .filePath(classMetrics.getFilePath())
                .totalScore(totalScore)
                .complexityScore(complexityScore)
                .couplingScore(couplingScore)
                .locScore(locScore)
                .nestingScore(nestingScore)
                .methodCountScore(methodCountScore)
                .debtLevel(debtLevel)
                .estimatedFixHours(estimatedFixHours)
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 分析整个项目的技术债务
     */
    public List<TechnicalDebtScore> analyzeProject(List<ClassMetrics> classMetricsList) {
        log.info("开始分析项目技术债务，共 {} 个类", classMetricsList.size());

        List<TechnicalDebtScore> scores = classMetricsList.stream()
                .map(this::analyzeClass)
                .collect(Collectors.toList());

        // 检测循环依赖
        detectCircularDependencies(classMetricsList, scores);

        log.info("项目技术债务分析完成");

        return scores;
    }

    /**
     * 生成重构建议
     */
    public List<RefactoringSuggestion> generateSuggestions(TechnicalDebtScore score, ClassMetrics classMetrics) {
        List<RefactoringSuggestion> suggestions = new ArrayList<>();

        // 圈复杂度建议
        if (score.getComplexityScore() > 40) {
            generateComplexitySuggestions(classMetrics, suggestions);
        }

        // 耦合度建议
        if (score.getCouplingScore() > 40) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.REDUCE_COUPLING)
                    .priority(score.getCouplingScore() > 70 ? RefactoringSuggestion.Priority.CRITICAL : RefactoringSuggestion.Priority.HIGH)
                    .className(classMetrics.getClassName())
                    .packageName(classMetrics.getPackageName())
                    .description(String.format("类 %s 耦合度过高 (CBO=%d)", classMetrics.getClassName(), classMetrics.getCouplingBetweenObjects()))
                    .currentValue(classMetrics.getCouplingBetweenObjects())
                    .targetValue(CBO_MEDIUM)
                    .impactScore(score.getCouplingScore())
                    .suggestion("考虑引入接口隔离依赖、使用依赖注入、或者将功能拆分到独立的类中")
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }

        // 类规模建议
        if (score.getLocScore() > 50) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.SPLIT_LARGE_CLASS)
                    .priority(score.getLocScore() > 80 ? RefactoringSuggestion.Priority.CRITICAL : RefactoringSuggestion.Priority.HIGH)
                    .className(classMetrics.getClassName())
                    .packageName(classMetrics.getPackageName())
                    .description(String.format("类 %s 过大，共 %d 行代码", classMetrics.getClassName(), classMetrics.getLinesOfCode()))
                    .currentValue(classMetrics.getLinesOfCode())
                    .targetValue(LOC_MEDIUM)
                    .impactScore(score.getLocScore())
                    .suggestion("考虑将大类拆分为多个职责单一的小类，遵循单一职责原则")
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }

        // 方法数量建议
        if (score.getMethodCountScore() > 40) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.SPLIT_LARGE_CLASS)
                    .priority(score.getMethodCountScore() > 70 ? RefactoringSuggestion.Priority.HIGH : RefactoringSuggestion.Priority.MEDIUM)
                    .className(classMetrics.getClassName())
                    .packageName(classMetrics.getPackageName())
                    .description(String.format("类 %s 方法过多，共 %d 个方法", classMetrics.getClassName(), classMetrics.getTotalMethods()))
                    .currentValue(classMetrics.getTotalMethods())
                    .targetValue(METHODS_MANY)
                    .impactScore(score.getMethodCountScore())
                    .suggestion("将相关方法分组并提取到新的类中，或者考虑使用组合模式")
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }

        // 方法级别建议
        for (ComplexityMetrics method : classMetrics.getMethods()) {
            generateMethodLevelSuggestions(method, suggestions);
        }

        // 按优先级排序
        suggestions.sort((s1, s2) -> s2.getPriority().compareTo(s1.getPriority()));

        return suggestions;
    }

    /**
     * 计算圈复杂度得分 (0-100)
     */
    private double calculateComplexityScore(ClassMetrics classMetrics) {
        if (classMetrics.getMethods().isEmpty()) {
            return 0;
        }

        double totalScore = 0;
        int count = 0;

        for (ComplexityMetrics method : classMetrics.getMethods()) {
            int cc = method.getCyclomaticComplexity();
            double score = 0;

            if (cc <= CC_SIMPLE) {
                score = 0;
            } else if (cc <= CC_MODERATE) {
                score = ((cc - CC_SIMPLE) / (double)(CC_MODERATE - CC_SIMPLE)) * 40;
            } else if (cc <= CC_COMPLEX) {
                score = 40 + ((cc - CC_MODERATE) / (double)(CC_COMPLEX - CC_MODERATE)) * 30;
            } else {
                score = 70 + Math.min((cc - CC_COMPLEX) / 10.0 * 30, 30);
            }

            totalScore += score;
            count++;
        }

        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 计算耦合度得分 (0-100)
     */
    private double calculateCouplingScore(ClassMetrics classMetrics) {
        int cbo = classMetrics.getCouplingBetweenObjects();

        if (cbo <= CBO_LOW) {
            return 0;
        } else if (cbo <= CBO_MEDIUM) {
            return ((cbo - CBO_LOW) / (double)(CBO_MEDIUM - CBO_LOW)) * 40;
        } else if (cbo <= CBO_HIGH) {
            return 40 + ((cbo - CBO_MEDIUM) / (double)(CBO_HIGH - CBO_MEDIUM)) * 40;
        } else {
            return 80 + Math.min((cbo - CBO_HIGH) / 10.0 * 20, 20);
        }
    }

    /**
     * 计算代码行数得分 (0-100)
     */
    private double calculateLocScore(ClassMetrics classMetrics) {
        int loc = classMetrics.getLinesOfCode();

        if (loc <= LOC_SMALL) {
            return 0;
        } else if (loc <= LOC_MEDIUM) {
            return ((loc - LOC_SMALL) / (double)(LOC_MEDIUM - LOC_SMALL)) * 30;
        } else if (loc <= LOC_LARGE) {
            return 30 + ((loc - LOC_MEDIUM) / (double)(LOC_LARGE - LOC_MEDIUM)) * 40;
        } else {
            return 70 + Math.min((loc - LOC_LARGE) / 500.0 * 30, 30);
        }
    }

    /**
     * 计算嵌套深度得分 (0-100)
     */
    private double calculateNestingScore(ClassMetrics classMetrics) {
        if (classMetrics.getMethods().isEmpty()) {
            return 0;
        }

        double totalScore = 0;
        int count = 0;

        for (ComplexityMetrics method : classMetrics.getMethods()) {
            int nesting = method.getNestingDepth();
            double score = 0;

            if (nesting <= NESTING_SHALLOW) {
                score = 0;
            } else if (nesting <= NESTING_DEEP) {
                score = ((nesting - NESTING_SHALLOW) / (double)(NESTING_DEEP - NESTING_SHALLOW)) * 50;
            } else if (nesting <= NESTING_VERY_DEEP) {
                score = 50 + ((nesting - NESTING_DEEP) / (double)(NESTING_VERY_DEEP - NESTING_DEEP)) * 30;
            } else {
                score = 80 + Math.min((nesting - NESTING_VERY_DEEP) * 4, 20);
            }

            totalScore += score;
            count++;
        }

        return count > 0 ? totalScore / count : 0;
    }

    /**
     * 计算方法数量得分 (0-100)
     */
    private double calculateMethodCountScore(ClassMetrics classMetrics) {
        int methods = classMetrics.getTotalMethods();

        if (methods <= METHODS_FEW) {
            return 0;
        } else if (methods <= METHODS_MANY) {
            return ((methods - METHODS_FEW) / (double)(METHODS_MANY - METHODS_FEW)) * 40;
        } else if (methods <= METHODS_TOO_MANY) {
            return 40 + ((methods - METHODS_MANY) / (double)(METHODS_TOO_MANY - METHODS_MANY)) * 40;
        } else {
            return 80 + Math.min((methods - METHODS_TOO_MANY) / 10.0 * 20, 20);
        }
    }

    /**
     * 计算预估修复工时
     */
    private int calculateEstimatedFixHours(double totalScore) {
        if (totalScore < 20) {
            return 0;
        } else if (totalScore < 40) {
            return (int)(totalScore / 10);
        } else if (totalScore < 60) {
            return (int)(totalScore / 5);
        } else if (totalScore < 80) {
            return (int)(totalScore / 3);
        } else {
            return (int)(totalScore / 2);
        }
    }

    /**
     * 生成复杂度相关建议
     */
    private void generateComplexitySuggestions(ClassMetrics classMetrics, List<RefactoringSuggestion> suggestions) {
        // 找出复杂度最高的方法
        classMetrics.getMethods().stream()
                .filter(m -> m.getCyclomaticComplexity() > CC_MODERATE)
                .sorted((m1, m2) -> Integer.compare(m2.getCyclomaticComplexity(), m1.getCyclomaticComplexity()))
                .limit(3)
                .forEach(method -> {
                    suggestions.add(RefactoringSuggestion.builder()
                            .type(RefactoringSuggestion.SuggestionType.REDUCE_COMPLEXITY)
                            .priority(method.getCyclomaticComplexity() > CC_COMPLEX ? RefactoringSuggestion.Priority.CRITICAL : RefactoringSuggestion.Priority.HIGH)
                            .className(classMetrics.getClassName())
                            .packageName(classMetrics.getPackageName())
                            .methodName(method.getMethodName())
                            .description(String.format("方法 %s 圈复杂度过高 (CC=%d)", method.getMethodName(), method.getCyclomaticComplexity()))
                            .currentValue(method.getCyclomaticComplexity())
                            .targetValue(CC_MODERATE)
                            .impactScore(method.getCyclomaticComplexity() / CC_COMPLEX * 100)
                            .suggestion("考虑提取方法、使用策略模式或状态模式来降低复杂度")
                            .codeExample(generateComplexityExample())
                            .analysisTimestamp(System.currentTimeMillis())
                            .build());
                });
    }

    /**
     * 生成方法级别建议
     */
    private void generateMethodLevelSuggestions(ComplexityMetrics method, List<RefactoringSuggestion> suggestions) {
        // 嵌套深度建议
        if (method.getNestingDepth() > NESTING_DEEP) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.REDUCE_NESTING)
                    .priority(method.getNestingDepth() > NESTING_VERY_DEEP ? RefactoringSuggestion.Priority.HIGH : RefactoringSuggestion.Priority.MEDIUM)
                    .className(method.getClassName())
                    .packageName(method.getPackageName())
                    .methodName(method.getMethodName())
                    .description(String.format("方法 %s 嵌套过深 (深度=%d)", method.getMethodName(), method.getNestingDepth()))
                    .currentValue(method.getNestingDepth())
                    .targetValue(NESTING_DEEP)
                    .impactScore(method.getNestingDepth() / NESTING_VERY_DEEP * 100)
                    .suggestion("考虑使用卫语句(guard clauses)、提取方法或使用提前返回来减少嵌套")
                    .codeExample(generateNestingExample())
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }

        // 参数数量建议
        if (method.getNumberOfParameters() > PARAMS_MANY) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.REDUCE_PARAMETERS)
                    .priority(method.getNumberOfParameters() > PARAMS_TOO_MANY ? RefactoringSuggestion.Priority.HIGH : RefactoringSuggestion.Priority.MEDIUM)
                    .className(method.getClassName())
                    .packageName(method.getPackageName())
                    .methodName(method.getMethodName())
                    .description(String.format("方法 %s 参数过多 (参数=%d)", method.getMethodName(), method.getNumberOfParameters()))
                    .currentValue(method.getNumberOfParameters())
                    .targetValue(PARAMS_MANY)
                    .impactScore(method.getNumberOfParameters() / PARAMS_TOO_MANY * 100)
                    .suggestion("考虑引入参数对象(parameter object)或使用构建器模式")
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }

        // 局部变量建议
        if (method.getNumberOfLocalVariables() > VARIABLES_MANY) {
            suggestions.add(RefactoringSuggestion.builder()
                    .type(RefactoringSuggestion.SuggestionType.REDUCE_VARIABLES)
                    .priority(method.getNumberOfLocalVariables() > VARIABLES_TOO_MANY ? RefactoringSuggestion.Priority.MEDIUM : RefactoringSuggestion.Priority.LOW)
                    .className(method.getClassName())
                    .packageName(method.getPackageName())
                    .methodName(method.getMethodName())
                    .description(String.format("方法 %s 局部变量过多 (变量=%d)", method.getMethodName(), method.getNumberOfLocalVariables()))
                    .currentValue(method.getNumberOfLocalVariables())
                    .targetValue(VARIABLES_MANY)
                    .impactScore(method.getNumberOfLocalVariables() / VARIABLES_TOO_MANY * 100)
                    .suggestion("考虑提取方法或使用新的类来封装相关变量")
                    .analysisTimestamp(System.currentTimeMillis())
                    .build());
        }
    }

    /**
     * 检测循环依赖
     */
    private void detectCircularDependencies(List<ClassMetrics> classMetricsList, List<TechnicalDebtScore> scores) {
        // 构建依赖图
        Map<String, Set<String>> dependencyGraph = new HashMap<>();

        for (ClassMetrics classMetrics : classMetricsList) {
            String className = classMetrics.getClassName();
            Set<String> dependencies = new HashSet<>(classMetrics.getDependencies());
            dependencyGraph.put(className, dependencies);
        }

        // 使用 DFS 检测循环
        for (String className : dependencyGraph.keySet()) {
            Set<String> visited = new HashSet<>();
            Set<String> recursionStack = new HashSet<>();
            List<String> cycle = new ArrayList<>();

            if (detectCycleDFS(className, dependencyGraph, visited, recursionStack, cycle)) {
                // 为循环依赖中的类添加建议
                for (String cycleClass : cycle) {
                    Optional<TechnicalDebtScore> scoreOpt = scores.stream()
                            .filter(s -> s.getClassName().equals(cycleClass))
                            .findFirst();

                    scoreOpt.ifPresent(score -> {
                        score.setCircularDependency(true);
                        // 循环依赖作为重构建议返回，这里需要通过其他方式传递
                    });
                }
            }
        }
    }

    /**
     * DFS 检测循环依赖
     */
    private boolean detectCycleDFS(String node, Map<String, Set<String>> graph,
                                    Set<String> visited, Set<String> recursionStack,
                                    List<String> cycle) {
        visited.add(node);
        recursionStack.add(node);

        for (String neighbor : graph.getOrDefault(node, Collections.emptySet())) {
            if (!visited.contains(neighbor)) {
                if (detectCycleDFS(neighbor, graph, visited, recursionStack, cycle)) {
                    return true;
                }
            } else if (recursionStack.contains(neighbor)) {
                // 找到循环
                cycle.add(neighbor);
                return true;
            }
        }

        recursionStack.remove(node);
        return false;
    }

    /**
     * 生成复杂度示例代码
     */
    private String generateComplexityExample() {
        return """
                // 重构前：高圈复杂度
                public void process(Order order) {
                    if (order != null) {
                        if (order.getType().equals("STANDARD")) {
                            if (order.getAmount() > 1000) {
                                // 标准订单高金额处理
                            } else {
                                // 标准订单低金额处理
                            }
                        } else if (order.getType().equals("EXPRESS")) {
                            if (order.isUrgent()) {
                                // 紧急快速订单处理
                            } else {
                                // 普通快速订单处理
                            }
                        }
                    }
                }

                // 重构后：使用策略模式降低复杂度
                public void process(Order order) {
                    OrderProcessor processor = OrderProcessorFactory.create(order);
                    processor.process(order);
                }
                """;
    }

    /**
     * 生成嵌套示例代码
     */
    private String generateNestingExample() {
        return """
                // 重构前：深嵌套
                public void validateUser(User user) {
                    if (user != null) {
                        if (user.isActive()) {
                            if (user.hasPermission()) {
                                if (user.isVerified()) {
                                    // 执行操作
                                }
                            }
                        }
                    }
                }

                // 重构后：使用卫语句
                public void validateUser(User user) {
                    if (user == null) return;
                    if (!user.isActive()) return;
                    if (!user.hasPermission()) return;
                    if (!user.isVerified()) return;

                    // 执行操作
                }
                """;
    }
}