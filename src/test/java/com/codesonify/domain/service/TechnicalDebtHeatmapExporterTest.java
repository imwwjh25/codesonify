package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.TechnicalDebtScore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 技术债务热力图导出器测试
 */
@DisplayName("技术债务热力图导出器测试")
class TechnicalDebtHeatmapExporterTest {

    private TechnicalDebtHeatmapExporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new TechnicalDebtHeatmapExporter();
    }

    @Test
    @DisplayName("导出热力图 - 应该生成有效的 XML")
    void exportToDrawio_ShouldGenerateValidXml() {
        // Given
        List<TechnicalDebtScore> scores = createSampleScores(5);

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertNotNull(xml);
        assertFalse(xml.isEmpty(), "生成的 XML 不应该为空");
        assertTrue(xml.contains("<mxfile"), "应该包含 mxfile 根元素");
        assertTrue(xml.contains("<mxGraphModel"), "应该包含 mxGraphModel 元素");
        assertTrue(xml.contains("技术债务热力图"), "应该包含标题");
    }

    @Test
    @DisplayName("导出热力图 - 应该包含所有类的信息")
    void exportToDrawio_ShouldContainAllClassInformation() {
        // Given
        List<TechnicalDebtScore> scores = createSampleScores(3);

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        for (TechnicalDebtScore score : scores) {
            assertTrue(xml.contains(score.getClassName()),
                    "应该包含类名: " + score.getClassName());
            assertTrue(xml.contains(String.format("%.1f", score.getTotalScore())),
                    "应该包含分数: " + score.getTotalScore());
        }
    }

    @Test
    @DisplayName("导出热力图 - 应该包含图例")
    void exportToDrawio_ShouldContainLegend() {
        // Given
        List<TechnicalDebtScore> scores = createSampleScores(1);

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertTrue(xml.contains("无债务 (0-20)"), "应该包含无债务图例");
        assertTrue(xml.contains("低债务 (20-40)"), "应该包含低债务图例");
        assertTrue(xml.contains("中等债务 (40-60)"), "应该包含中等债务图例");
        assertTrue(xml.contains("高债务 (60-80)"), "应该包含高债务图例");
        assertTrue(xml.contains("严重债务 (80-100)"), "应该包含严重债务图例");
    }

    @Test
    @DisplayName("导出热力图 - 应该包含统计信息")
    void exportToDrawio_ShouldContainStatistics() {
        // Given
        List<TechnicalDebtScore> scores = createSampleScores(10);

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertTrue(xml.contains("统计信息"), "应该包含统计信息标题");
        assertTrue(xml.contains("总类数"), "应该包含总类数统计");
        assertTrue(xml.contains("平均分"), "应该包含平均分统计");
        assertTrue(xml.contains("预估修复工时"), "应该包含预估修复工时统计");
    }

    @Test
    @DisplayName("导出热力图 - 不同等级应该使用不同颜色")
    void exportToDrawio_ShouldUseDifferentColorsForDifferentLevels() {
        // Given
        List<TechnicalDebtScore> scores = List.of(
                createScoreWithLevel("Class1", TechnicalDebtScore.TechnicalDebtLevel.NONE),
                createScoreWithLevel("Class2", TechnicalDebtScore.TechnicalDebtLevel.LOW),
                createScoreWithLevel("Class3", TechnicalDebtScore.TechnicalDebtLevel.MEDIUM),
                createScoreWithLevel("Class4", TechnicalDebtScore.TechnicalDebtLevel.HIGH),
                createScoreWithLevel("Class5", TechnicalDebtScore.TechnicalDebtLevel.CRITICAL)
        );

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertTrue(xml.contains("#00C853"), "应该包含绿色（无债务）");
        assertTrue(xml.contains("#64DD17"), "应该包含浅绿色（低债务）");
        assertTrue(xml.contains("#FFD600"), "应该包含黄色（中等债务）");
        assertTrue(xml.contains("#FF6D00"), "应该包含橙色（高债务）");
        assertTrue(xml.contains("#D50000"), "应该包含红色（严重债务）");
    }

    @Test
    @DisplayName("导出热力图 - 空列表应该生成有效的 XML")
    void exportToDrawio_ShouldGenerateValidXmlForEmptyList() {
        // Given
        List<TechnicalDebtScore> scores = List.of();

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertNotNull(xml);
        assertFalse(xml.isEmpty());
        assertTrue(xml.contains("<mxfile"), "空列表也应该生成有效的 XML 结构");
    }

    @Test
    @DisplayName("导出热力图 - 长类名应该被截断")
    void exportToDrawio_ShouldTruncateLongClassNames() {
        // Given
        String longClassName = "VeryVeryVeryVeryVeryVeryLongClassNameThatExceedsTwelveCharacters";
        List<TechnicalDebtScore> scores = List.of(
                createScore("Class1", 10),
                createScore(longClassName, 50)
        );

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertTrue(xml.contains("Class1"), "应该包含短类名");
        // 长类名在工具提示中会完整显示，但单元格中会截断
        assertTrue(xml.contains("..."), "应该包含截断标记");
        // 验证单元格显示的截断名称（截断为10字符 + ...）
        assertTrue(xml.contains("VeryVeryVe..."), "应该包含截断后的类名");
    }

    @Test
    @DisplayName("导出热力图 - 应该包含工具提示")
    void exportToDrawio_ShouldContainTooltips() {
        // Given
        List<TechnicalDebtScore> scores = List.of(
                createScore("TestClass", 50)
        );

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertTrue(xml.contains("tooltip-"), "应该包含工具提示元素");
    }

    @Test
    @DisplayName("导出热力图 - XML 应该正确转义特殊字符")
    void exportToDrawio_ShouldEscapeSpecialCharacters() {
        // Given
        List<TechnicalDebtScore> scores = List.of(
                createScore("Class<>&\"'", 50)
        );

        // When
        String xml = exporter.exportToDrawio(scores);

        // Then
        assertFalse(xml.contains("<&>"), "应该转义特殊字符");
        assertTrue(xml.contains("&lt;"), "应该包含 &lt; 转义");
        assertTrue(xml.contains("&gt;"), "应该包含 &gt; 转义");
    }

    /**
     * 创建示例分数列表
     */
    private List<TechnicalDebtScore> createSampleScores(int count) {
        List<TechnicalDebtScore> scores = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            double score = 10 + i * 15;
            TechnicalDebtScore.TechnicalDebtLevel level = TechnicalDebtScore.determineLevel(score);
            scores.add(createScoreWithLevel("Class" + i, level));
        }
        return scores;
    }

    /**
     * 创建指定等级的分数
     */
    private TechnicalDebtScore createScoreWithLevel(String className,
                                                      TechnicalDebtScore.TechnicalDebtLevel level) {
        double score = switch (level) {
            case NONE -> 10;
            case LOW -> 30;
            case MEDIUM -> 50;
            case HIGH -> 70;
            case CRITICAL -> 90;
        };
        return createScore(className, score);
    }

    /**
     * 创建分数
     */
    private TechnicalDebtScore createScore(String className, double score) {
        return TechnicalDebtScore.builder()
                .className(className)
                .packageName("com.example")
                .filePath("/path/to/" + className + ".java")
                .totalScore(score)
                .complexityScore(score * 0.3)
                .couplingScore(score * 0.2)
                .locScore(score * 0.2)
                .nestingScore(score * 0.15)
                .methodCountScore(score * 0.15)
                .debtLevel(TechnicalDebtScore.determineLevel(score))
                .estimatedFixHours((int) (score / 5))
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }
}