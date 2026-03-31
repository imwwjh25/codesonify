package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.MethodType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 方法分析器
 *
 * 分析单个方法的各项指标
 */
@Slf4j
@Service
public class MethodAnalyzer {

    private final CyclomaticComplexityCalculator complexityCalculator;

    public MethodAnalyzer(CyclomaticComplexityCalculator complexityCalculator) {
        this.complexityCalculator = complexityCalculator;
    }

    /**
     * 分析方法
     *
     * @param method 方法声明
     * @param className 类名
     * @param packageName 包名
     * @param filePath 文件路径
     * @return 方法复杂度指标
     */
    public ComplexityMetrics analyzeMethod(MethodDeclaration method, String className,
                                           String packageName, String filePath) {
        log.debug("分析方法：{}.{}", className, method.getNameAsString());

        return complexityCalculator.analyzeMethod(method, className, packageName, filePath);
    }

    /**
     * 分析类中的所有方法
     *
     * @param clazz 类声明
     * @param packageName 包名
     * @param filePath 文件路径
     * @return 方法指标列表
     */
    public List<ComplexityMetrics> analyzeMethods(ClassOrInterfaceDeclaration clazz,
                                                   String packageName, String filePath) {
        List<ComplexityMetrics> metricsList = new ArrayList<>();

        for (MethodDeclaration method : clazz.getMethods()) {
            try {
                ComplexityMetrics metrics = analyzeMethod(
                        method,
                        clazz.getNameAsString(),
                        packageName,
                        filePath
                );
                metricsList.add(metrics);
            } catch (Exception e) {
                log.warn("分析方法失败：{}.{} - {}", clazz.getNameAsString(),
                        method.getNameAsString(), e.getMessage());
            }
        }

        log.debug("分析了 {} 个方法", metricsList.size());
        return metricsList;
    }

    /**
     * 判断是否为 Getter 方法
     */
    public boolean isGetter(MethodDeclaration method) {
        String name = method.getNameAsString();
        return (name.startsWith("get") || name.startsWith("is"))
                && method.getParameters().isEmpty();
    }

    /**
     * 判断是否为 Setter 方法
     */
    public boolean isSetter(MethodDeclaration method) {
        String name = method.getNameAsString();
        return name.startsWith("set")
                && method.getParameters().size() == 1
                && method.getType().toString().equals("void");
    }

    /**
     * 判断是否为构造函数
     */
    public boolean isConstructor(MethodDeclaration method) {
        return method.getNameAsString().equals("<init>");
    }

    /**
     * 判断是否为业务方法（非 getter/setter/构造函数）
     */
    public boolean isBusinessMethod(MethodDeclaration method) {
        return !isGetter(method) && !isSetter(method) && !isConstructor(method);
    }
}
