package com.codesonify.domain.service;

import com.codesonify.domain.entity.MethodType;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 复杂度到音乐的映射器
 *
 * 将代码复杂度指标映射为音乐参数：
 * - 圈复杂度 → 音高（Pitch）
 * - 代码行数 → 音符时长（Duration）
 * - 嵌套深度 → 音量（Velocity）
 * - 方法类型 → 音色（Instrument）
 * - 耦合度 → 和声（Chord）
 */
@Service
public class ComplexityToMusicMapper {

    /**
     * 基础音高（C4 = 60）
     */
    private static final int BASE_NOTE = 60;

    /**
     * 将圈复杂度映射为 MIDI 音高
     *
     * 映射规则：
     * - 复杂度 1-5: C4-G4 (60-67) - 低音区，大调音阶
     * - 复杂度 6-10: A4-E5 (69-76) - 中音区
     * - 复杂度 11-20: F5-C6 (77-84) - 高音区
     * - 复杂度 >20: 不和谐音程
     *
     * @param complexity 圈复杂度
     * @return MIDI 音高值 (0-127)
     */
    public int complexityToPitch(int complexity) {
        if (complexity <= 5) {
            // 简单：低音区，使用大调音阶
            // C, D, E, F, G 对应 60, 62, 64, 65, 67
            return BASE_NOTE + (complexity - 1) * 2;
        } else if (complexity <= 10) {
            // 中等：中音区，从 A4 开始
            return BASE_NOTE + 9 + (complexity - 6) * 1;
        } else if (complexity <= 20) {
            // 复杂：高音区
            return BASE_NOTE + 17 + (complexity - 11) / 2;
        } else {
            // 非常复杂：使用不和谐音程（小二度）
            return BASE_NOTE + 24 + (complexity % 12);
        }
    }

    /**
     * 将代码行数映射为音符时长（秒）
     *
     * @param linesOfCode 代码行数
     * @return 音符时长（秒）
     */
    public double linesToDuration(int linesOfCode) {
        if (linesOfCode < 20) return 0.25;      // 短方法
        if (linesOfCode < 50) return 0.5;       // 中等方法
        if (linesOfCode < 100) return 1.0;      // 长方法
        return 2.0;                              // 超长方法
    }

    /**
     * 将嵌套深度映射为音量（MIDI velocity: 0-127）
     *
     * @param nestingDepth 嵌套深度
     * @return MIDI velocity 值
     */
    public int depthToVelocity(int nestingDepth) {
        int baseVelocity = 60;
        int increment = 15;
        return Math.min(127, baseVelocity + nestingDepth * increment);
    }

    /**
     * 根据方法类型选择音色（MIDI instrument）
     *
     * @param type 方法类型
     * @return MIDI 乐器编号
     */
    public int methodTypeToInstrument(MethodType type) {
        return switch (type) {
            case CONSTRUCTOR -> 0;        // Acoustic Grand Piano
            case GETTER, SETTER -> 24;    // Acoustic Guitar
            case BUSINESS_LOGIC -> 40;    // Violin
            case UTILITY -> 73;           // Flute
            case TEST -> 118;             // Synth Drum
        };
    }

    /**
     * 根据耦合度生成和弦
     *
     * @param coupling 耦合度
     * @param basePitch 基础音高
     * @return 和弦音符列表
     */
    public List<Integer> couplingToChord(int coupling, int basePitch) {
        List<Integer> chord = new ArrayList<>();

        if (coupling <= 2) {
            // 低耦合：单音
            chord.add(basePitch);
        } else if (coupling <= 5) {
            // 中耦合：大三和弦
            chord.add(basePitch);
            chord.add(basePitch + 4);  // 大三度
            chord.add(basePitch + 7);  // 纯五度
        } else {
            // 高耦合：不和谐和弦
            chord.add(basePitch);
            chord.add(basePitch + 6);  // 增四度（三全音）
            chord.add(basePitch + 10); // 小七度
        }

        return chord;
    }

    /**
     * 将复杂度等级映射为调式
     *
     * @param complexity 圈复杂度
     * @return 调式名称
     */
    public String complexityToMode(int complexity) {
        if (complexity <= 5) {
            return "C 大调";  // 简单：明亮的大调
        } else if (complexity <= 10) {
            return "A 小调";  // 中等：柔和的小调
        } else if (complexity <= 20) {
            return "D 多利亚调式";  // 复杂
        } else {
            return "无调性";  // 非常复杂：无调性音乐
        }
    }

    /**
     * 获取音符名称
     *
     * @param midiNote MIDI 音高值
     * @return 音符名称（如 C4, E5）
     */
    public String getNoteName(int midiNote) {
        String[] notes = {"C", "C#", "D", "D#", "E", "F", "F#", "G", "G#", "A", "A#", "B"};
        int octave = (midiNote / 12) - 1;
        String noteName = notes[midiNote % 12];
        return noteName + octave;
    }

    /**
     * 将复杂度指标转换为音乐描述
     *
     * @param complexity 圈复杂度
     * @param lines 代码行数
     * @param depth 嵌套深度
     * @param methodType 方法类型
     * @return 音乐描述字符串
     */
    public String toMusicDescription(int complexity, int lines, int depth, MethodType methodType) {
        int pitch = complexityToPitch(complexity);
        double duration = linesToDuration(lines);
        int velocity = depthToVelocity(depth);
        int instrument = methodTypeToInstrument(methodType);

        return String.format("音高：%s (%d), 时长：%.2f 秒，音量：%d, 乐器：%d",
                getNoteName(pitch), pitch, duration, velocity, instrument);
    }
}
