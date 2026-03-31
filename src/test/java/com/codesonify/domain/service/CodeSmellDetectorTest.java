package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * CodeSmellDetector 单元测试
 */
class CodeSmellDetectorTest {

    private CodeSmellDetector detector;

    @BeforeEach
    void setUp() {
        detector = new CodeSmellDetector();
        // 设置较低的阈值以便测试
        detector.setLongMethodMaxLines(30);
        detector.setLongMethodMaxNestingDepth(3);
        detector.setLongMethodMaxComplexity(5);
        detector.setLargeClassMaxLines(200);
        detector.setLargeClassMaxMethods(10);
        detector.setLargeClassMaxFields(15);
        detector.setLongParameterListMaxParams(4);
        detector.setHighCouplingMaxCBO(8);
    }

    @Test
    void testDetectLongMethod_ByLines() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("longMethod")
                .linesOfCode(50)
                .cyclomaticComplexity(3)
                .nestingDepth(2)
                .numberOfParameters(2)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongMethod(methodMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LONG_METHOD, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("50"));
    }

    @Test
    void testDetectLongMethod_ByNestingDepth() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("nestedMethod")
                .linesOfCode(20)
                .cyclomaticComplexity(3)
                .nestingDepth(5)
                .numberOfParameters(2)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongMethod(methodMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LONG_METHOD, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("嵌套深度"));
    }

    @Test
    void testDetectLongMethod_ByComplexity() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("complexMethod")
                .linesOfCode(20)
                .cyclomaticComplexity(15)
                .nestingDepth(2)
                .numberOfParameters(2)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongMethod(methodMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LONG_METHOD, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("圈复杂度"));
    }

    @Test
    void testDetectNoLongMethod() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("simpleMethod")
                .linesOfCode(10)
                .cyclomaticComplexity(2)
                .nestingDepth(1)
                .numberOfParameters(2)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongMethod(methodMetrics);

        assertFalse(smell.isPresent());
    }

    @Test
    void testDetectLargeClass_ByLines() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("LargeClass")
                .packageName("com.example")
                .filePath("/path/to/LargeClass.java")
                .linesOfCode(300)
                .totalMethods(5)
                .numberOfFields(10)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(5)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLargeClass(classMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LARGE_CLASS, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("行数"));
    }

    @Test
    void testDetectLargeClass_ByMethods() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("ManyMethodsClass")
                .packageName("com.example")
                .filePath("/path/to/ManyMethodsClass.java")
                .linesOfCode(100)
                .totalMethods(20)
                .numberOfFields(10)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(5)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLargeClass(classMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LARGE_CLASS, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("方法数"));
    }

    @Test
    void testDetectLargeClass_ByFields() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("ManyFieldsClass")
                .packageName("com.example")
                .filePath("/path/to/ManyFieldsClass.java")
                .linesOfCode(100)
                .totalMethods(5)
                .numberOfFields(25)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(5)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLargeClass(classMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LARGE_CLASS, smell.get().getType());
        assertTrue(smell.get().getMessage().contains("字段数"));
    }

    @Test
    void testDetectLongParameterList() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("parameterHeavyMethod")
                .linesOfCode(20)
                .cyclomaticComplexity(2)
                .nestingDepth(1)
                .numberOfParameters(6)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongParameterList(methodMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.LONG_PARAMETER_LIST, smell.get().getType());
    }

    @Test
    void testDetectNoLongParameterList() {
        ComplexityMetrics methodMetrics = ComplexityMetrics.builder()
                .className("TestClass")
                .methodName("cleanMethod")
                .linesOfCode(20)
                .cyclomaticComplexity(2)
                .nestingDepth(1)
                .numberOfParameters(2)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectLongParameterList(methodMetrics);

        assertFalse(smell.isPresent());
    }

    @Test
    void testDetectHighCoupling() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("HighCouplingClass")
                .packageName("com.example")
                .filePath("/path/to/HighCouplingClass.java")
                .linesOfCode(100)
                .totalMethods(5)
                .numberOfFields(10)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(15)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectHighCoupling(classMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.HIGH_COUPLING, smell.get().getType());
    }

    @Test
    void testDetectGodClass() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("GodClass")
                .packageName("com.example")
                .filePath("/path/to/GodClass.java")
                .linesOfCode(300)
                .totalMethods(20)
                .numberOfFields(25)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(15)
                .build();

        java.util.Optional<CodeSmell> smell = detector.detectGodClass(classMetrics);

        assertTrue(smell.isPresent());
        assertEquals(SmellType.GOD_CLASS, smell.get().getType());
        assertEquals(Severity.CRITICAL, smell.get().getSeverity());
    }

    @Test
    void testDetectCodeSmells() {
        ClassMetrics classMetrics = ClassMetrics.builder()
                .className("ProblematicClass")
                .packageName("com.example")
                .filePath("/path/to/ProblematicClass.java")
                .linesOfCode(300)
                .totalMethods(20)
                .numberOfFields(25)
                .methods(new ArrayList<>())
                .dependencies(new ArrayList<>())
                .couplingBetweenObjects(15)
                .build();

        List<ClassMetrics> classMetricsList = List.of(classMetrics);

        List<CodeSmell> codeSmells = detector.detectCodeSmells(classMetricsList);

        assertFalse(codeSmells.isEmpty());
        assertTrue(codeSmells.stream().anyMatch(s -> s.getType() == SmellType.LARGE_CLASS));
        assertTrue(codeSmells.stream().anyMatch(s -> s.getType() == SmellType.GOD_CLASS));
    }
}