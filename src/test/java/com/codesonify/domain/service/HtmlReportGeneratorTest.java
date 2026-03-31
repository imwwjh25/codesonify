package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.DependencyGraph;
import com.codesonify.domain.entity.ProjectAnalysis;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * HtmlReportGenerator 单元测试
 */
class HtmlReportGeneratorTest {

    private HtmlReportGenerator reportGenerator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        reportGenerator = new HtmlReportGenerator();
    }

    @Test
    @DisplayName("生成 HTML 报告")
    void testGenerateHtmlReport() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        assertDoesNotThrow(() -> {
            reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists(), "HTML 文件应该被创建");
        assertTrue(outputFile.length() > 0, "文件应该有内容");
    }

    @Test
    @DisplayName("生成的 HTML 包含正确的结构")
    void testGenerateHtmlReport_ValidStructure() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        // 验证 HTML 结构
        assertTrue(content.contains("<!DOCTYPE html>"), "应该是 HTML5 格式");
        assertTrue(content.contains("<html"), "应该包含 html 标签");
        assertTrue(content.contains("</html>"), "应该正确关闭 html 标签");
        assertTrue(content.contains("<head>"), "应该包含 head 标签");
        assertTrue(content.contains("<body>"), "应该包含 body 标签");
    }

    @Test
    @DisplayName("生成的 HTML 包含项目信息")
    void testGenerateHtmlReport_ProjectInfo() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("TestProject"), "应该包含项目名称");
        assertTrue(content.contains("项目信息"), "应该包含项目信息部分");
    }

    @Test
    @DisplayName("生成的 HTML 包含统计信息")
    void testGenerateHtmlReport_Statistics() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("总类数"), "应该包含总类数标签");
        assertTrue(content.contains("总方法数"), "应该包含总方法数标签");
        assertTrue(content.contains("平均复杂度"), "应该包含平均复杂度标签");
    }

    @Test
    @DisplayName("生成的 HTML 包含 Chart.js 图表")
    void testGenerateHtmlReport_Chart() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("cdn.jsdelivr.net/npm/chart.js"), "应该包含 Chart.js CDN");
        assertTrue(content.contains("<canvas id=\"complexityChart\">"), "应该包含图表 canvas");
        assertTrue(content.contains("new Chart"), "应该包含 Chart.js 初始化代码");
    }

    @Test
    @DisplayName("生成的 HTML 包含类列表表格")
    void testGenerateHtmlReport_ClassTable() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("<table"), "应该包含表格");
        assertTrue(content.contains("TestClassA"), "应该包含类名 TestClassA");
        assertTrue(content.contains("TestClassB"), "应该包含类名 TestClassB");
    }

    @Test
    @DisplayName("生成的 HTML 包含 MIDI 播放器")
    void testGenerateHtmlReport_WithMusicPlayer() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();
        String midiBase64 = "dGVzdCBtaWRpIGZpbGU="; // "test midi file"

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath(), midiBase64);

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("代码音乐"), "应该包含代码音乐部分");
        assertTrue(content.contains("<audio controls>"), "应该包含音频播放器");
        assertTrue(content.contains("data:audio/midi;base64"), "应该包含 MIDI 数据 URL");
    }

    @Test
    @DisplayName("生成的 HTML 不带 MIDI 播放器（无 base64）")
    void testGenerateHtmlReport_WithoutMusicPlayer() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertFalse(content.contains("<audio controls>"), "不应该包含音频播放器");
    }

    @Test
    @DisplayName("生成的 HTML 包含响应式 CSS")
    void testGenerateHtmlReport_ResponsiveCSS() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("@media"), "应该包含媒体查询");
        assertTrue(content.contains("max-width"), "应该包含响应式断点");
    }

    @Test
    @DisplayName("生成的 HTML 包含排序功能")
    void testGenerateHtmlReport_Sortable() throws Exception {
        ProjectAnalysis analysis = createTestAnalysis();
        File outputFile = tempDir.resolve("report.html").toFile();

        reportGenerator.generateHtmlReport(analysis, outputFile.getAbsolutePath());

        String content = Files.readString(outputFile.toPath());

        assertTrue(content.contains("onclick=\"sortTable"), "应该包含排序点击事件");
        assertTrue(content.contains("function sortTable"), "应该包含排序函数");
    }

    /**
     * 创建测试用的 ProjectAnalysis
     */
    private ProjectAnalysis createTestAnalysis() {
        ProjectAnalysis analysis = new ProjectAnalysis();
        analysis.setProjectName("TestProject");
        analysis.setProjectPath("/test/project");
        analysis.setAnalysisTime(LocalDateTime.now());

        // 创建测试类
        List<ClassMetrics> classes = List.of(
                createClassMetrics("TestClassA", 5, 100, 2),
                createClassMetrics("TestClassB", 10, 200, 5)
        );
        analysis.setClasses(classes);

        // 创建依赖图
        DependencyGraph graph = new DependencyGraph();
        graph.addNode("TestClassA");
        graph.addNode("TestClassB");
        graph.addEdge("TestClassA", "TestClassB");
        graph.calculateMetrics();
        analysis.setDependencyGraph(graph);

        // 创建统计信息
        ProjectAnalysis.Statistics stats = new ProjectAnalysis.Statistics();
        stats.setTotalClasses(2);
        stats.setTotalMethods(10);
        stats.setAverageCyclomaticComplexity(7.5);
        stats.setMaxCyclomaticComplexity(10);
        stats.setTotalLinesOfCode(300);
        stats.setAverageCoupling(3.5);
        stats.setCycleCount(0);

        // 创建复杂度分布
        ProjectAnalysis.Statistics.ComplexityDistribution distribution =
                new ProjectAnalysis.Statistics.ComplexityDistribution();
        distribution.setSimple(5);
        distribution.setModerate(3);
        distribution.setComplex(2);
        distribution.setVeryComplex(0);
        stats.setComplexityDistribution(distribution);

        analysis.setStatistics(stats);

        return analysis;
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
                .totalMethods(5)
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
