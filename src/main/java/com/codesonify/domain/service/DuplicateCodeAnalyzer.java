package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import lombok.Data;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 重复代码分析器
 *
 * 检测代码中的重复片段，基于 AST 相似度分析
 */
@Service
public class DuplicateCodeAnalyzer {

    // 默认配置
    private static final int MIN_BLOCK_SIZE = 6; // 最小代码块大小（语句数）
    private static final double DEFAULT_SIMILARITY_THRESHOLD = 0.9; // 默认相似度阈值

    private int minBlockSize = MIN_BLOCK_SIZE;
    private double similarityThreshold = DEFAULT_SIMILARITY_THRESHOLD;

    /**
     * 重复代码块
     */
    @Data
    public static class DuplicateCodeBlock {
        private String className1;
        private String methodName1;
        private int lineNumber1;
        private String className2;
        private String methodName2;
        private int lineNumber2;
        private double similarity;
        private String codeSnippet;
    }

    /**
     * 分析重复代码
     *
     * @param classMetricsList 类指标列表
     * @return 重复代码块列表
     */
    public List<DuplicateCodeBlock> analyzeDuplicates(List<ClassMetrics> classMetricsList) {
        List<DuplicateCodeBlock> duplicates = new ArrayList<>();

        // 收集所有方法代码
        List<MethodCodeInfo> methodCodeInfos = new ArrayList<>();
        for (ClassMetrics classMetrics : classMetricsList) {
            for (ComplexityMetrics methodMetrics : classMetrics.getMethods()) {
                MethodCodeInfo info = new MethodCodeInfo();
                info.setClassName(classMetrics.getClassName());
                info.setMethodName(methodMetrics.getMethodName());
                info.setFilePath(classMetrics.getFilePath());
                info.setPackageName(classMetrics.getPackageName());

                // 尝试从文件解析获取方法代码
                try {
                    File file = new File(classMetrics.getFilePath());
                    if (file.exists()) {
                        JavaParser parser = new JavaParser();
                        CompilationUnit cu = parser.parse(new FileInputStream(file)).getResult().orElse(null);
                        if (cu != null) {
                            Optional<MethodDeclaration> methodOpt = cu.findFirst(MethodDeclaration.class,
                                    m -> m.getNameAsString().equals(methodMetrics.getMethodName()));
                            if (methodOpt.isPresent()) {
                                info.setCode(methodOpt.get().toString());
                                info.setLineNumber(methodOpt.get().getRange()
                                        .map(r -> r.begin.line).orElse(0));
                            }
                        }
                    }
                } catch (Exception e) {
                    // 忽略解析错误
                }

                methodCodeInfos.add(info);
            }
        }

        // 计算方法之间的相似度
        for (int i = 0; i < methodCodeInfos.size(); i++) {
            for (int j = i + 1; j < methodCodeInfos.size(); j++) {
                MethodCodeInfo info1 = methodCodeInfos.get(i);
                MethodCodeInfo info2 = methodCodeInfos.get(j);

                // 跳过同一个类的方法
                if (info1.getClassName().equals(info2.getClassName())) {
                    continue;
                }

                double similarity = calculateSimilarity(info1.getCode(), info2.getCode());
                if (similarity >= similarityThreshold) {
                    DuplicateCodeBlock block = new DuplicateCodeBlock();
                    block.setClassName1(info1.getClassName());
                    block.setMethodName1(info1.getMethodName());
                    block.setLineNumber1(info1.getLineNumber());
                    block.setClassName2(info2.getClassName());
                    block.setMethodName2(info2.getMethodName());
                    block.setLineNumber2(info2.getLineNumber());
                    block.setSimilarity(similarity);
                    block.setCodeSnippet(truncateCode(info1.getCode(), 200));

                    duplicates.add(block);
                }
            }
        }

        return duplicates;
    }

    /**
     * 将重复代码转换为代码异味列表
     *
     * @param duplicates 重复代码块列表
     * @return 代码异味列表
     */
    public List<CodeSmell> convertToCodeSmells(List<DuplicateCodeBlock> duplicates) {
        List<CodeSmell> codeSmells = new ArrayList<>();

        for (DuplicateCodeBlock duplicate : duplicates) {
            Severity severity = calculateSeverity(duplicate.getSimilarity());

            // 为第一个位置创建异味
            CodeSmell smell1 = CodeSmell.create(
                    SmellType.DUPLICATE_CODE,
                    severity,
                    duplicate.getClassName1(),
                    duplicate.getMethodName1(),
                    duplicate.getLineNumber1(),
                    String.format("方法 %s.%s 与 %s.%s 存在重复代码，相似度 %.2f%%",
                            duplicate.getClassName1(),
                            duplicate.getMethodName1(),
                            duplicate.getClassName2(),
                            duplicate.getMethodName2(),
                            duplicate.getSimilarity() * 100),
                    String.format("建议提取公共方法或使用模板方法模式消除重复"),
                    duplicate.getSimilarity(),
                    similarityThreshold
            );
            codeSmells.add(smell1);

            // 为第二个位置创建异味
            CodeSmell smell2 = CodeSmell.create(
                    SmellType.DUPLICATE_CODE,
                    severity,
                    duplicate.getClassName2(),
                    duplicate.getMethodName2(),
                    duplicate.getLineNumber2(),
                    String.format("方法 %s.%s 与 %s.%s 存在重复代码，相似度 %.2f%%",
                            duplicate.getClassName2(),
                            duplicate.getMethodName2(),
                            duplicate.getClassName1(),
                            duplicate.getMethodName1(),
                            duplicate.getSimilarity() * 100),
                    String.format("建议提取公共方法或使用模板方法模式消除重复"),
                    duplicate.getSimilarity(),
                    similarityThreshold
            );
            codeSmells.add(smell2);
        }

        return codeSmells;
    }

    /**
     * 计算代码相似度（基于 Token 相似度）
     *
     * @param code1 代码1
     * @param code2 代码2
     * @return 相似度 (0.0 - 1.0)
     */
    private double calculateSimilarity(String code1, String code2) {
        if (code1 == null || code2 == null || code1.isEmpty() || code2.isEmpty()) {
            return 0.0;
        }

        // 提取 token
        List<String> tokens1 = extractTokens(code1);
        List<String> tokens2 = extractTokens(code2);

        if (tokens1.isEmpty() || tokens2.isEmpty()) {
            return 0.0;
        }

        // 计算最长公共子序列长度
        int lcsLength = longestCommonSubsequence(tokens1, tokens2);
        int maxLength = Math.max(tokens1.size(), tokens2.size());

        return (double) lcsLength / maxLength;
    }

    /**
     * 提取代码 token
     *
     * @param code 代码
     * @return token 列表
     */
    private List<String> extractTokens(String code) {
        // 简单的 token 提取：按空白和标点符号分割
        return Arrays.stream(code.split("[\\s\\{\\}\\(\\)\\[\\];,<>=!\\+\\-\\*/\\|&]+"))
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    /**
     * 计算最长公共子序列长度
     *
     * @param list1 列表1
     * @param list2 列表2
     * @return LCS 长度
     */
    private <T> int longestCommonSubsequence(List<T> list1, List<T> list2) {
        int m = list1.size();
        int n = list2.size();

        int[][] dp = new int[m + 1][n + 1];

        for (int i = 1; i <= m; i++) {
            for (int j = 1; j <= n; j++) {
                if (list1.get(i - 1).equals(list2.get(j - 1))) {
                    dp[i][j] = dp[i - 1][j - 1] + 1;
                } else {
                    dp[i][j] = Math.max(dp[i - 1][j], dp[i][j - 1]);
                }
            }
        }

        return dp[m][n];
    }

    /**
     * 截断代码
     *
     * @param code 代码
     * @param maxLength 最大长度
     * @return 截断后的代码
     */
    private String truncateCode(String code, int maxLength) {
        if (code == null || code.length() <= maxLength) {
            return code;
        }
        return code.substring(0, maxLength) + "...";
    }

    /**
     * 根据相似度计算严重程度
     *
     * @param similarity 相似度
     * @return 严重程度
     */
    private Severity calculateSeverity(double similarity) {
        if (similarity >= 0.99) {
            return Severity.CRITICAL;
        } else if (similarity >= 0.95) {
            return Severity.HIGH;
        } else if (similarity >= 0.9) {
            return Severity.MEDIUM;
        } else {
            return Severity.LOW;
        }
    }

    /**
     * 方法代码信息
     */
    @Data
    private static class MethodCodeInfo {
        private String className;
        private String methodName;
        private String filePath;
        private String packageName;
        private String code;
        private int lineNumber;
    }

    // Getter 和 Setter
    public int getMinBlockSize() {
        return minBlockSize;
    }

    public void setMinBlockSize(int minBlockSize) {
        this.minBlockSize = minBlockSize;
    }

    public double getSimilarityThreshold() {
        return similarityThreshold;
    }

    public void setSimilarityThreshold(double similarityThreshold) {
        this.similarityThreshold = similarityThreshold;
    }
}