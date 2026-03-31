package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityMetrics;
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
 * 类分析器
 *
 * 分析整个类的各项指标
 */
@Slf4j
@Service
public class ClassAnalyzer {

    private final MethodAnalyzer methodAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;

    public ClassAnalyzer(MethodAnalyzer methodAnalyzer, DependencyAnalyzer dependencyAnalyzer) {
        this.methodAnalyzer = methodAnalyzer;
        this.dependencyAnalyzer = dependencyAnalyzer;
    }

    /**
     * 分析类
     *
     * @param cu 编译单元
     * @param filePath 文件路径
     * @return 类指标
     */
    public ClassMetrics analyzeClass(CompilationUnit cu, String filePath) {
        return cu.getPrimaryTypeName()
                .map(className -> analyzeClass(cu, className, filePath))
                .orElse(null);
    }

    /**
     * 分析类
     *
     * @param cu 编译单元
     * @param className 类名
     * @param filePath 文件路径
     * @return 类指标
     */
    public ClassMetrics analyzeClass(CompilationUnit cu, String className, String filePath) {
        log.info("分析类：{}", className);

        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        // 查找类声明
        ClassOrInterfaceDeclaration clazz = cu.findFirst(ClassOrInterfaceDeclaration.class,
                c -> c.getNameAsString().equals(className)).orElse(null);

        if (clazz == null) {
            log.warn("未找到类声明：{}", className);
            return null;
        }

        // 分析方法
        List<ComplexityMetrics> methods = methodAnalyzer.analyzeMethods(clazz, packageName, filePath);

        // 计算统计指标
        int totalMethods = methods.size();
        double averageComplexity = methods.stream()
                .mapToInt(ComplexityMetrics::getCyclomaticComplexity)
                .average()
                .orElse(0.0);
        int maxComplexity = methods.stream()
                .mapToInt(ComplexityMetrics::getCyclomaticComplexity)
                .max()
                .orElse(0);
        int linesOfCode = calculateClassLinesOfCode(clazz);
        int couplingBetweenObjects = dependencyAnalyzer.calculateCBO(clazz, cu);

        // 计算字段数量
        int numberOfFields = clazz.getFields().size();

        // 计算构造函数数量
        int numberOfConstructors = (int) clazz.getMethods().stream()
                .filter(m -> m.getNameAsString().equals("<init>"))
                .count();

        // 计算静态方法数量
        int numberOfStaticMethods = (int) clazz.getMethods().stream()
                .filter(m -> m.isStatic())
                .count();

        return ClassMetrics.builder()
                .className(className)
                .packageName(packageName)
                .filePath(filePath)
                .totalMethods(totalMethods)
                .averageComplexity(averageComplexity)
                .maxComplexity(maxComplexity)
                .linesOfCode(linesOfCode)
                .couplingBetweenObjects(couplingBetweenObjects)
                .methods(methods)
                .dependencies(dependencyAnalyzer.extractDependencies(cu))
                .numberOfFields(numberOfFields)
                .numberOfConstructors(numberOfConstructors)
                .numberOfStaticMethods(numberOfStaticMethods)
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 计算类的代码行数
     */
    private int calculateClassLinesOfCode(ClassOrInterfaceDeclaration clazz) {
        return clazz.getEnd().isPresent() && clazz.getBegin().isPresent()
                ? clazz.getEnd().get().line - clazz.getBegin().get().line + 1
                : 0;
    }

    /**
     * 分析多个类
     *
     * @param compilationUnits 编译单元列表
     * @return 类指标列表
     */
    public List<ClassMetrics> analyzeClasses(List<CompilationUnit> compilationUnits) {
        List<ClassMetrics> allMetrics = new ArrayList<>();

        for (CompilationUnit cu : compilationUnits) {
            String filePath = cu.getStorage().map(s -> s.getPath().toString()).orElse("");
            cu.getPrimaryTypeName().ifPresent(className -> {
                ClassMetrics metrics = analyzeClass(cu, className, filePath);
                if (metrics != null) {
                    allMetrics.add(metrics);
                }
            });
        }

        log.info("分析了 {} 个类", allMetrics.size());
        return allMetrics;
    }
}
