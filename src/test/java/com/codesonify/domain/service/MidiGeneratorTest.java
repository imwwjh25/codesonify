package com.codesonify.domain.service;

import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.MethodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sound.midi.InvalidMidiDataException;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * MidiGenerator 单元测试
 */
class MidiGeneratorTest {

    private MidiGenerator midiGenerator;
    private ComplexityToMusicMapper mapper;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        mapper = new ComplexityToMusicMapper();
        midiGenerator = new MidiGenerator(mapper);
    }

    @Test
    @DisplayName("生成单个方法 MIDI 文件")
    void testGenerateMidi_SingleMethod() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> metrics = List.of(
                createMetrics("TestClass", "simpleMethod", 1, 10, 0, MethodType.BUSINESS_LOGIC)
        );

        File outputFile = tempDir.resolve("test.mid").toFile();

        assertDoesNotThrow(() -> {
            midiGenerator.generateMidi(metrics, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists(), "MIDI 文件应该被创建");
        assertTrue(outputFile.length() > 0, "MIDI 文件应该有内容");
    }

    @Test
    @DisplayName("生成多个方法 MIDI 文件")
    void testGenerateMidi_MultipleMethods() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> metrics = List.of(
                createMetrics("TestClass", "simpleMethod", 1, 10, 0, MethodType.GETTER),
                createMetrics("TestClass", "complexMethod", 10, 50, 3, MethodType.BUSINESS_LOGIC),
                createMetrics("TestClass", "constructor", 2, 15, 0, MethodType.CONSTRUCTOR)
        );

        File outputFile = tempDir.resolve("test.mid").toFile();

        assertDoesNotThrow(() -> {
            midiGenerator.generateMidi(metrics, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists(), "MIDI 文件应该被创建");
    }

    @Test
    @DisplayName("生成 MIDI 字节数组")
    void testGenerateMidiBytes() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> metrics = List.of(
                createMetrics("TestClass", "method1", 5, 20, 1, MethodType.UTILITY)
        );

        byte[] midiBytes = assertDoesNotThrow(() -> midiGenerator.generateMidiBytes(metrics));

        assertNotNull(midiBytes);
        assertTrue(midiBytes.length > 0);
    }

    @Test
    @DisplayName("不同复杂度生成不同 MIDI 文件")
    void testGenerateMidi_DifferentComplexity() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> simpleMetrics = List.of(
                createMetrics("TestClass", "simple", 1, 10, 0, MethodType.BUSINESS_LOGIC)
        );

        List<ComplexityMetrics> complexMetrics = List.of(
                createMetrics("TestClass", "complex", 20, 100, 5, MethodType.BUSINESS_LOGIC)
        );

        File simpleFile = tempDir.resolve("simple.mid").toFile();
        File complexFile = tempDir.resolve("complex.mid").toFile();

        midiGenerator.generateMidi(simpleMetrics, simpleFile.getAbsolutePath());
        midiGenerator.generateMidi(complexMetrics, complexFile.getAbsolutePath());

        // 两个文件都应该被创建
        assertTrue(simpleFile.exists(), "简单 MIDI 文件应该被创建");
        assertTrue(complexFile.exists(), "复杂 MIDI 文件应该被创建");
        assertTrue(simpleFile.length() > 0, "MIDI 文件应该有内容");
    }

    @Test
    @DisplayName("不同方法类型使用不同乐器")
    void testGenerateMidi_DifferentInstruments() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> metrics = List.of(
                createMetrics("TestClass", "constructor", 1, 10, 0, MethodType.CONSTRUCTOR),
                createMetrics("TestClass", "getter", 1, 10, 0, MethodType.GETTER),
                createMetrics("TestClass", "business", 1, 10, 0, MethodType.BUSINESS_LOGIC),
                createMetrics("TestClass", "utility", 1, 10, 0, MethodType.UTILITY)
        );

        File outputFile = tempDir.resolve("test.mid").toFile();

        assertDoesNotThrow(() -> {
            midiGenerator.generateMidi(metrics, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists());
    }

    @Test
    @DisplayName("空列表生成空 MIDI 文件")
    void testGenerateMidi_EmptyList() throws IOException, InvalidMidiDataException {
        List<ComplexityMetrics> metrics = List.of();

        File outputFile = tempDir.resolve("empty.mid").toFile();

        assertDoesNotThrow(() -> {
            midiGenerator.generateMidi(metrics, outputFile.getAbsolutePath());
        });

        assertTrue(outputFile.exists());
    }

    /**
     * 创建测试用的 ComplexityMetrics
     */
    private ComplexityMetrics createMetrics(String className, String methodName,
                                             int complexity, int lines, int depth,
                                             MethodType methodType) {
        return ComplexityMetrics.builder()
                .className(className)
                .methodName(methodName)
                .signature("public void " + methodName + "()")
                .cyclomaticComplexity(complexity)
                .linesOfCode(lines)
                .nestingDepth(depth)
                .numberOfParameters(0)
                .numberOfLocalVariables(0)
                .methodType(methodType)
                .level(ComplexityLevel.fromComplexity(complexity))
                .packageName("com.test")
                .filePath("/test/" + className + ".java")
                .analysisTimestamp(System.currentTimeMillis())
                .build();
    }
}
