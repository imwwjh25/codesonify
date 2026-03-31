package com.codesonify.domain.service;

import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeSmellAggregator 单元测试
 */
class CodeSmellAggregatorTest {

    private CodeSmellAggregator aggregator;
    private List<CodeSmell> codeSmells;

    @BeforeEach
    void setUp() {
        aggregator = new CodeSmellAggregator();
        codeSmells = createTestCodeSmells();
    }

    @Test
    void testAggregateByType() {
        Map<SmellType, List<CodeSmell>> byType = aggregator.aggregateByType(codeSmells);

        assertEquals(3, byType.get(SmellType.LONG_METHOD).size());
        assertEquals(1, byType.get(SmellType.LARGE_CLASS).size()); // 修正：只有1个LARGE_CLASS
        assertEquals(1, byType.get(SmellType.HIGH_COUPLING).size());
        assertNotNull(byType.get(SmellType.GOD_CLASS)); // GOD_CLASS应该存在
        assertEquals(1, byType.get(SmellType.GOD_CLASS).size());
        assertNull(byType.get(SmellType.DUPLICATE_CODE)); // DUPLICATE_CODE不存在
    }

    @Test
    void testAggregateBySeverity() {
        Map<Severity, List<CodeSmell>> bySeverity = aggregator.aggregateBySeverity(codeSmells);

        assertEquals(1, bySeverity.get(Severity.CRITICAL).size());
        assertEquals(2, bySeverity.get(Severity.HIGH).size());
        assertEquals(2, bySeverity.get(Severity.MEDIUM).size());
        assertEquals(1, bySeverity.get(Severity.LOW).size());
    }

    @Test
    void testAggregateByClass() {
        Map<String, List<CodeSmell>> byClass = aggregator.aggregateByClass(codeSmells);

        assertEquals(3, byClass.get("ClassA").size());
        assertEquals(2, byClass.get("ClassB").size());
        assertEquals(1, byClass.get("ClassC").size());
    }

    @Test
    void testCalculateScore() {
        double score = aggregator.calculateScore(codeSmells);

        assertTrue(score > 0);
        assertTrue(score <= 100);
    }

    @Test
    void testCalculateScore_EmptyList() {
        double score = aggregator.calculateScore(new ArrayList<>());

        assertEquals(0.0, score);
    }

    @Test
    void testCalculateQualityScore() {
        double qualityScore = aggregator.calculateQualityScore(codeSmells);

        assertTrue(qualityScore >= 0);
        assertTrue(qualityScore <= 100);
    }

    @Test
    void testCalculateQualityScore_Complement() {
        double smellScore = aggregator.calculateScore(codeSmells);
        double qualityScore = aggregator.calculateQualityScore(codeSmells);

        assertEquals(100, smellScore + qualityScore, 0.01);
    }

    @Test
    void testGenerateStatistics() {
        CodeSmellAggregator.CodeSmellStatistics stats = aggregator.generateStatistics(codeSmells);

        assertEquals(6, stats.getTotalCount());
        assertEquals(1, stats.getCriticalCount());
        assertEquals(2, stats.getHighCount());
        assertEquals(2, stats.getMediumCount());
        assertEquals(1, stats.getLowCount());
        assertTrue(stats.getAverageScore() > 0);
        assertNotNull(stats.getTypeDistribution());
        assertNotNull(stats.getSeverityDistribution());
        assertNotNull(stats.getTopAffectedClasses());
        assertEquals("ClassA", stats.getTopAffectedClasses().get(0)); // ClassA 有 3 个异味
    }

    @Test
    void testFilterByType() {
        List<CodeSmell> longMethods = aggregator.filterByType(codeSmells, SmellType.LONG_METHOD);

        assertEquals(3, longMethods.size());
        assertTrue(longMethods.stream().allMatch(s -> s.getType() == SmellType.LONG_METHOD));
    }

    @Test
    void testFilterBySeverity() {
        List<CodeSmell> criticalSmells = aggregator.filterBySeverity(codeSmells, Severity.CRITICAL);

        assertEquals(1, criticalSmells.size());
        assertEquals(Severity.CRITICAL, criticalSmells.get(0).getSeverity());
    }

    @Test
    void testFilterByClass() {
        List<CodeSmell> classASmells = aggregator.filterByClass(codeSmells, "ClassA");

        assertEquals(3, classASmells.size());
        assertTrue(classASmells.stream().allMatch(s -> s.getClassName().equals("ClassA")));
    }

    @Test
    void testGetQualityLevel() {
        String level = aggregator.getQualityLevel(codeSmells);

        assertNotNull(level);
        assertTrue(List.of("优秀", "良好", "中等", "及格", "需要改进").contains(level));
    }

    @Test
    void testGetQualityLevel_Perfect() {
        List<CodeSmell> emptyList = new ArrayList<>();
        String level = aggregator.getQualityLevel(emptyList);

        assertEquals("优秀", level);
    }

    @Test
    void testGetQualityLevel_Poor() {
        List<CodeSmell> poorSmells = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            poorSmells.add(CodeSmell.create(
                    SmellType.GOD_CLASS,
                    Severity.CRITICAL,
                    "BadClass",
                    null,
                    0,
                    "Test",
                    "Test",
                    100,
                    50
            ));
        }

        String level = aggregator.getQualityLevel(poorSmells);

        assertEquals("需要改进", level);
    }

    /**
     * 创建测试用的代码异味列表
     */
    private List<CodeSmell> createTestCodeSmells() {
        List<CodeSmell> smells = new ArrayList<>();

        // ClassA - 3 个异味（1 严重，1 高，1 中）
        smells.add(CodeSmell.create(
                SmellType.GOD_CLASS, Severity.CRITICAL, "ClassA", null, 0,
                "上帝类", "重构", 100, 50));
        smells.add(CodeSmell.create(
                SmellType.LONG_METHOD, Severity.HIGH, "ClassA", "method1", 0,
                "长方法", "拆分", 100, 50));
        smells.add(CodeSmell.create(
                SmellType.HIGH_COUPLING, Severity.MEDIUM, "ClassA", null, 0,
                "高耦合", "解耦", 15, 10));

        // ClassB - 2 个异味（1 高，1 低）
        smells.add(CodeSmell.create(
                SmellType.LONG_METHOD, Severity.HIGH, "ClassB", "method2", 0,
                "长方法", "拆分", 80, 50));
        smells.add(CodeSmell.create(
                SmellType.LARGE_CLASS, Severity.LOW, "ClassB", null, 0,
                "大类", "拆分", 310, 300));

        // ClassC - 1 个异味（中）
        smells.add(CodeSmell.create(
                SmellType.LONG_METHOD, Severity.MEDIUM, "ClassC", "method3", 0,
                "长方法", "拆分", 60, 50));

        return smells;
    }
}