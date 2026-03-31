package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.DependencyGraph;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DependencyGraphExporter 单元测试
 */
class DependencyGraphExporterTest {

    private DependencyGraphExporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new DependencyGraphExporter();
    }

    @Test
    @DisplayName("导出空依赖图到 Draw.io")
    void testExportToDrawio_EmptyGraph() throws Exception {
        DependencyGraph graph = new DependencyGraph();
        File outputFile = tempDir.resolve("empty.drawio").toFile();

        assertDoesNotThrow(() -> {
            exporter.exportToDrawio(graph, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists(), "Draw.io 文件应该被创建");
        assertTrue(outputFile.length() > 0, "文件应该有内容");
    }

    @Test
    @DisplayName("导出带复杂度信息的依赖图")
    void testExportToDrawioWithMetrics() throws Exception {
        // 创建测试依赖图
        DependencyGraph graph = new DependencyGraph();
        graph.addNode("ClassA");
        graph.addNode("ClassB");
        graph.addNode("ClassC");
        graph.addEdge("ClassA", "ClassB");
        graph.addEdge("ClassB", "ClassC");
        graph.calculateMetrics();

        // 创建测试指标
        List<ClassMetrics> classMetrics = List.of(
                createClassMetrics("ClassA", 5, 100, 2),
                createClassMetrics("ClassB", 10, 200, 5),
                createClassMetrics("ClassC", 20, 300, 8)
        );

        File outputFile = tempDir.resolve("dependencies.drawio").toFile();

        assertDoesNotThrow(() -> {
            exporter.exportToDrawioWithMetrics(graph, classMetrics, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists(), "Draw.io 文件应该被创建");

        // 验证文件内容包含 XML 头
        String content = Files.readString(outputFile.toPath());
        assertTrue(content.contains("<?xml"), "文件应该是 XML 格式");
        assertTrue(content.contains("<mxfile"), "应该是 Draw.io 格式");
        assertTrue(content.contains("ClassA"), "应该包含类名 ClassA");
        assertTrue(content.contains("ClassB"), "应该包含类名 ClassB");
        assertTrue(content.contains("ClassC"), "应该包含类名 ClassC");
    }

    @Test
    @DisplayName("不同复杂度使用不同颜色样式")
    void testExportToDrawioWithDifferentComplexity() throws Exception {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode("Simple");
        graph.addNode("Moderate");
        graph.addNode("Complex");
        graph.addNode("VeryComplex");
        graph.calculateMetrics();

        List<ClassMetrics> classMetrics = List.of(
                createClassMetrics("Simple", 3, 50, 1),        // 简单 - 绿色
                createClassMetrics("Moderate", 8, 100, 3),     // 中等 - 黄色
                createClassMetrics("Complex", 15, 200, 5),     // 复杂 - 橙色
                createClassMetrics("VeryComplex", 25, 500, 10) // 非常复杂 - 红色
        );

        File outputFile = tempDir.resolve("colored.drawio").toFile();

        assertDoesNotThrow(() -> {
            exporter.exportToDrawioWithMetrics(graph, classMetrics, outputFile.getAbsolutePath());
        });

        String content = Files.readString(outputFile.toPath());

        // 验证不同颜色样式
        assertTrue(content.contains("fillColor=#d5e8d4"), "简单类应该使用绿色背景");
        assertTrue(content.contains("fillColor=#fff2cc"), "中等类应该使用黄色背景");
        assertTrue(content.contains("fillColor=#f8cecc"), "复杂类应该使用橙红色背景");
    }

    @Test
    @DisplayName("导出文件可以在 Draw.io 中打开")
    void testExportToDrawio_ValidFormat() throws Exception {
        DependencyGraph graph = new DependencyGraph();
        graph.addNode("TestClass");
        graph.calculateMetrics();

        List<ClassMetrics> classMetrics = List.of(
                createClassMetrics("TestClass", 5, 100, 2)
        );

        File outputFile = tempDir.resolve("valid.drawio").toFile();
        exporter.exportToDrawioWithMetrics(graph, classMetrics, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        // 验证 Draw.io 文件结构
        assertTrue(content.contains("<mxGraphModel"), "应该包含 mxGraphModel 根元素");
        assertTrue(content.contains("<root>"), "应该包含 root 元素");
        assertTrue(content.contains("<mxCell"), "应该包含 mxCell 元素");
        assertTrue(content.contains("</mxfile>"), "应该正确关闭 mxfile 标签");
    }

    /**
     * 创建测试用的 ClassMetrics
     */
    private ClassMetrics createClassMetrics(String className, int maxComplexity,
                                             int linesOfCode, int coupling) {
        ComplexityMetrics method = ComplexityMetrics.builder()
                .className(className)
                .methodName("testMethod")
                .signature("public void test()")
                .cyclomaticComplexity(maxComplexity)
                .linesOfCode(linesOfCode / 5)
                .nestingDepth(1)
                .numberOfParameters(0)
                .numberOfLocalVariables(0)
                .methodType(com.codesonify.domain.entity.MethodType.BUSINESS_LOGIC)
                .level(ComplexityLevel.fromComplexity(maxComplexity))
                .packageName("com.test")
                .filePath("/test/" + className + ".java")
                .analysisTimestamp(System.currentTimeMillis())
                .build();

        return ClassMetrics.builder()
                .className(className)
                .packageName("com.test")
                .filePath("/test/" + className + ".java")
                .totalMethods(1)
                .averageComplexity(maxComplexity)
                .maxComplexity(maxComplexity)
                .linesOfCode(linesOfCode)
                .couplingBetweenObjects(coupling)
                .methods(List.of(method))
                .dependencies(List.of())
                .numberOfFields(0)
                .numberOfConstructors(1)
                .numberOfStaticMethods(0)
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }
}
