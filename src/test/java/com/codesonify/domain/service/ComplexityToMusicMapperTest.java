package com.codesonify.domain.service;

import com.codesonify.domain.entity.MethodType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * ComplexityToMusicMapper 单元测试
 */
class ComplexityToMusicMapperTest {

    private ComplexityToMusicMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ComplexityToMusicMapper();
    }

    @Test
    @DisplayName("简单复杂度映射为低音区 (CC=1)")
    void testComplexityToPitch_Simple() {
        int pitch = mapper.complexityToPitch(1);

        assertEquals(60, pitch, "CC=1 应该映射为 C4 (60)");
    }

    @Test
    @DisplayName("简单复杂度映射为低音区 (CC=5)")
    void testComplexityToPitch_SimpleMax() {
        int pitch = mapper.complexityToPitch(5);

        assertEquals(68, pitch, "CC=5 应该映射为高音区");
    }

    @Test
    @DisplayName("中等复杂度映射为中音区 (CC=6)")
    void testComplexityToPitch_Moderate() {
        int pitch = mapper.complexityToPitch(6);

        assertTrue(pitch >= 69 && pitch <= 76, "CC=6-10 应该映射为中音区");
    }

    @Test
    @DisplayName("复杂代码映射为高音区 (CC=15)")
    void testComplexityToPitch_Complex() {
        int pitch = mapper.complexityToPitch(15);

        assertTrue(pitch >= 77, "CC=11-20 应该映射为高音区");
    }

    @Test
    @DisplayName("非常复杂代码映射为不和谐音程 (CC=25)")
    void testComplexityToPitch_VeryComplex() {
        int pitch = mapper.complexityToPitch(25);

        assertTrue(pitch > 84, "CC>20 应该映射为不和谐音程");
    }

    @Test
    @DisplayName("短方法映射为短时值")
    void testLinesToDuration_Short() {
        double duration = mapper.linesToDuration(10);

        assertEquals(0.25, duration, "<20 行应该映射为 0.25 秒");
    }

    @Test
    @DisplayName("中等方法映射为中等时值")
    void testLinesToDuration_Moderate() {
        double duration = mapper.linesToDuration(30);

        assertEquals(0.5, duration, "20-50 行应该映射为 0.5 秒");
    }

    @Test
    @DisplayName("长方法映射为长时值")
    void testLinesToDuration_Long() {
        double duration = mapper.linesToDuration(70);

        assertEquals(1.0, duration, "50-100 行应该映射为 1.0 秒");
    }

    @Test
    @DisplayName("超长方法映射为超长时值")
    void testLinesToDuration_VeryLong() {
        double duration = mapper.linesToDuration(150);

        assertEquals(2.0, duration, ">100 行应该映射为 2.0 秒");
    }

    @Test
    @DisplayName("浅层嵌套映射为中等音量")
    void testDepthToVelocity_Shallow() {
        int velocity = mapper.depthToVelocity(0);

        assertEquals(60, velocity, "深度 0 应该映射为基线音量 60");
    }

    @Test
    @DisplayName("深层嵌套映射为大音量")
    void testDepthToVelocity_Deep() {
        int velocity = mapper.depthToVelocity(4);

        assertEquals(120, velocity, "深度 4 应该映射为音量 120");
    }

    @Test
    @DisplayName("嵌套深度不超过最大 MIDI 值")
    void testDepthToVelocity_MaxCap() {
        int velocity = mapper.depthToVelocity(10);

        assertEquals(127, velocity, "音量不应超过 MIDI 最大值 127");
    }

    @Test
    @DisplayName("构造函数映射为钢琴音色")
    void testMethodTypeToInstrument_Constructor() {
        int instrument = mapper.methodTypeToInstrument(MethodType.CONSTRUCTOR);

        assertEquals(0, instrument, "构造函数应该使用钢琴 (0)");
    }

    @Test
    @DisplayName("Getter/Setter 映射为吉他音色")
    void testMethodTypeToInstrument_GetterSetter() {
        int instrument = mapper.methodTypeToInstrument(MethodType.GETTER);

        assertEquals(24, instrument, "Getter/Setter 应该使用吉他 (24)");
    }

    @Test
    @DisplayName("业务方法映射为小提琴音色")
    void testMethodTypeToInstrument_Business() {
        int instrument = mapper.methodTypeToInstrument(MethodType.BUSINESS_LOGIC);

        assertEquals(40, instrument, "业务方法应该使用小提琴 (40)");
    }

    @Test
    @DisplayName("工具方法映射为长笛音色")
    void testMethodTypeToInstrument_Utility() {
        int instrument = mapper.methodTypeToInstrument(MethodType.UTILITY);

        assertEquals(73, instrument, "工具方法应该使用长笛 (73)");
    }

    @Test
    @DisplayName("低耦合生成单音")
    void testCouplingToChord_Low() {
        var chord = mapper.couplingToChord(1, 60);

        assertEquals(1, chord.size(), "低耦合应该生成单音");
        assertEquals(60, chord.get(0));
    }

    @Test
    @DisplayName("中耦合生成大三和弦")
    void testCouplingToChord_Medium() {
        var chord = mapper.couplingToChord(3, 60);

        assertEquals(3, chord.size(), "中耦合应该生成大三和弦");
        assertEquals(60, chord.get(0));  // 根音
        assertEquals(64, chord.get(1));  // 大三度
        assertEquals(67, chord.get(2));  // 纯五度
    }

    @Test
    @DisplayName("高耦合生成不和谐和弦")
    void testCouplingToChord_High() {
        var chord = mapper.couplingToChord(10, 60);

        assertEquals(3, chord.size(), "高耦合应该生成不和谐和弦");
        assertEquals(60, chord.get(0));  // 根音
        assertEquals(66, chord.get(1));  // 增四度（三全音）
        assertEquals(70, chord.get(2));  // 小七度
    }

    @Test
    @DisplayName("简单代码映射为 C 大调")
    void testComplexityToMode_Simple() {
        String mode = mapper.complexityToMode(3);

        assertEquals("C 大调", mode);
    }

    @Test
    @DisplayName("中等代码映射为 A 小调")
    void testComplexityToMode_Moderate() {
        String mode = mapper.complexityToMode(8);

        assertEquals("A 小调", mode);
    }

    @Test
    @DisplayName("复杂代码映射为 D 多利亚调式")
    void testComplexityToMode_Complex() {
        String mode = mapper.complexityToMode(15);

        assertEquals("D 多利亚调式", mode);
    }

    @Test
    @DisplayName("非常复杂代码映射为无调性")
    void testComplexityToMode_VeryComplex() {
        String mode = mapper.complexityToMode(25);

        assertEquals("无调性", mode);
    }

    @Test
    @DisplayName("MIDI 音符转音符名称")
    void testGetNoteName() {
        assertEquals("C4", mapper.getNoteName(60));
        assertEquals("E4", mapper.getNoteName(64));
        assertEquals("G4", mapper.getNoteName(67));
        assertEquals("C5", mapper.getNoteName(72));
    }
}
