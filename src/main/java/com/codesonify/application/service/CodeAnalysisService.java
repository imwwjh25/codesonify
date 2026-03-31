package com.codesonify.application.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.DependencyGraph;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.domain.service.ClassAnalyzer;
import com.codesonify.domain.service.DependencyAnalyzer;
import com.codesonify.domain.service.JavaFileParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 代码分析应用服务
 *
 * 编排领域服务，提供代码分析的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeAnalysisService {

    private final JavaFileParser javaFileParser;
    private final ClassAnalyzer classAnalyzer;
    private final DependencyAnalyzer dependencyAnalyzer;

    /**
     * 分析整个项目
     *
     * @param projectPath 项目路径
     * @return 项目分析结果
     * @throws IOException 解析失败
     */
    public ProjectAnalysis analyzeProject(String projectPath) throws IOException {
        log.info("开始分析项目：{}", projectPath);

        Path path = Path.of(projectPath);

        // 解析所有 Java 文件
        var sourceFiles = javaFileParser.parseProject(path);
        var compilationUnits = sourceFiles.stream()
                .map(sf -> sf.getCompilationUnit())
                .toList();

        log.info("解析了 {} 个 Java 文件", sourceFiles.size());

        // 分析所有类
        List<ClassMetrics> classes = classAnalyzer.analyzeClasses(compilationUnits);

        // 构建依赖图
        DependencyGraph dependencyGraph = dependencyAnalyzer.analyzeDependencies(compilationUnits);

        // 构建项目分析结果
        ProjectAnalysis analysis = new ProjectAnalysis();
        analysis.setProjectName(path.getFileName().toString());
        analysis.setProjectPath(projectPath);
        analysis.setAnalysisTime(LocalDateTime.now());
        analysis.setClasses(classes);
        analysis.setDependencyGraph(dependencyGraph);
        analysis.setStatistics(calculateStatistics(classes, dependencyGraph));

        log.info("项目分析完成：{} 个类", classes.size());
        return analysis;
    }

    /**
     * 分析单个文件
     *
     * @param filePath 文件路径
     * @return 类指标
     * @throws IOException 解析失败
     */
    public ClassMetrics analyzeFile(String filePath) throws IOException {
        log.info("开始分析文件：{}", filePath);

        var sourceFile = javaFileParser.parseFileToSource(new java.io.File(filePath));
        ClassMetrics metrics = classAnalyzer.analyzeClass(
                sourceFile.getCompilationUnit(),
                sourceFile.getClassName(),
                filePath
        );

        log.info("文件分析完成：{}", sourceFile.getClassName());
        return metrics;
    }

    /**
     * 计算项目统计信息
     */
    private ProjectAnalysis.Statistics calculateStatistics(List<ClassMetrics> classes,
                                                            DependencyGraph graph) {
        ProjectAnalysis.Statistics stats = new ProjectAnalysis.Statistics();

        stats.setTotalClasses(classes.size());
        stats.setTotalMethods(classes.stream()
                .mapToInt(ClassMetrics::getTotalMethods)
                .sum());

        // 计算平均复杂度
        stats.setAverageCyclomaticComplexity(classes.stream()
                .flatMap(c -> c.getMethods().stream())
                .mapToInt(ClassMetrics::getAverageComplexity)
                .average()
                .orElse(0.0));

        // 计算最大复杂度
        stats.setMaxCyclomaticComplexity(classes.stream()
                .mapToInt(ClassMetrics::getMaxComplexity)
                .max()
                .orElse(0));

        // 计算总代码行数
        stats.setTotalLinesOfCode(classes.stream()
                .mapToInt(ClassMetrics::getLinesOfCode)
                .sum());

        // 计算平均耦合度
        stats.setAverageCoupling(classes.stream()
                .mapToInt(ClassMetrics::getCouplingBetweenObjects)
                .average()
                .orElse(0.0));

        // 循环依赖数量
        stats.setCycleCount(graph.getStatistics().getCycleCount());

        // 复杂度分布
        ProjectAnalysis.Statistics.ComplexityDistribution distribution =
                new ProjectAnalysis.Statistics.ComplexityDistribution();

        int simple = 0, moderate = 0, complex = 0, veryComplex = 0;

        for (ClassMetrics clazz : classes) {
            for (ClassMetrics method : clazz.getMethods()) {
                switch (method.getLevel()) {
                    case SIMPLE -> simple++;
                    case MODERATE -> moderate++;
                    case COMPLEX -> complex++;
                    case VERY_COMPLEX -> veryComplex++;
                }
            }
        }

        distribution.setSimple(simple);
        distribution.setModerate(moderate);
        distribution.setComplex(complex);
        distribution.setVeryComplex(veryComplex);

        stats.setComplexityDistribution(distribution);

        return stats;
    }
}
