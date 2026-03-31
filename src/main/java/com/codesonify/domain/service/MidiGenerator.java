package com.codesonify.domain.service;

import com.codesonify.domain.entity.ComplexityMetrics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.midi.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * MIDI 文件生成器
 *
 * 将代码复杂度指标转换为 MIDI 音乐文件
 */
@Slf4j
@Service
public class MidiGenerator {

    private static final int TICKS_PER_BEAT = 480;
    private static final int TEMPO = 120; // BPM

    private final ComplexityToMusicMapper mapper;

    public MidiGenerator(ComplexityToMusicMapper mapper) {
        this.mapper = mapper;
    }

    /**
     * 生成 MIDI 文件
     *
     * @param metrics 复杂度指标列表
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     * @throws InvalidMidiDataException MIDI 数据异常
     */
    public void generateMidi(List<ComplexityMetrics> metrics, String outputPath)
            throws IOException, InvalidMidiDataException {

        log.info("开始生成 MIDI 文件：{}", outputPath);

        // 处理空列表情况
        if (metrics == null || metrics.isEmpty()) {
            log.warn("指标列表为空，生成空 MIDI 文件");
            Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
            sequence.createTrack(); // 创建空音轨

            File midiFile = new File(outputPath);
            ensureParentDirectoryExists(midiFile);
            MidiSystem.write(sequence, 1, midiFile);
            return;
        }

        // 创建序列
        Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);

        // 按方法类型分组
        var grouped = groupMetricsByInstrument(metrics);

        for (var entry : grouped.entrySet()) {
            Track track = sequence.createTrack();
            int instrument = entry.getKey();
            List<ComplexityMetrics> methodMetrics = entry.getValue();

            // 设置乐器
            setInstrument(track, 0, instrument);

            // 添加音符
            long tickPosition = 0;
            for (ComplexityMetrics metric : methodMetrics) {
                int pitch = mapper.complexityToPitch(metric.getCyclomaticComplexity());
                int velocity = mapper.depthToVelocity(metric.getNestingDepth());
                double duration = mapper.linesToDuration(metric.getLinesOfCode());

                // 添加音符
                addNote(track, tickPosition, pitch, velocity, duration);

                // 移动到下一个位置
                tickPosition += ticksForDuration(duration);
            }
        }

        // 写入文件
        File midiFile = new File(outputPath);
        ensureParentDirectoryExists(midiFile);

        int result = MidiSystem.write(sequence, 1, midiFile);
        log.info("MIDI 文件已生成：{} ({} 字节)", outputPath, midiFile.length());
    }

    /**
     * 确保父目录存在
     */
    private void ensureParentDirectoryExists(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    /**
     * 按乐器分组方法指标
     */
    private java.util.Map<Integer, List<ComplexityMetrics>> groupMetricsByInstrument(
            List<ComplexityMetrics> metrics) {
        java.util.Map<Integer, List<ComplexityMetrics>> grouped = new java.util.HashMap<>();

        for (ComplexityMetrics metric : metrics) {
            int instrument = mapper.methodTypeToInstrument(metric.getMethodType());
            grouped.computeIfAbsent(instrument, k -> new ArrayList<>()).add(metric);
        }

        return grouped;
    }

    /**
     * 获取乐器数量
     */
    private int getInstrumentCount(List<ComplexityMetrics> metrics) {
        return (int) metrics.stream()
                .map(m -> mapper.methodTypeToInstrument(m.getMethodType()))
                .distinct()
                .count();
    }

    /**
     * 设置乐器
     *
     * @param track 音轨
     * @param tick 时间位置
     * @param instrument MIDI 乐器编号 (0-127)
     */
    private void setInstrument(Track track, long tick, int instrument) {
        // Program Change 事件：设置乐器
        ShortMessage programChange = new ShortMessage();
        try {
            programChange.setMessage(0xC0, 0, instrument);
            track.add(new MidiEvent(programChange, tick));
        } catch (InvalidMidiDataException e) {
            log.error("设置乐器失败：{}", instrument, e);
        }
    }

    /**
     * 添加音符
     *
     * @param track 音轨
     * @param tick 开始时间
     * @param pitch 音高
     * @param velocity 音量
     * @param duration 时长 (秒)
     */
    private void addNote(Track track, long tick, int pitch, int velocity, double duration) {
        try {
            // Note On 事件
            ShortMessage noteOn = new ShortMessage();
            noteOn.setMessage(0x90, pitch, velocity);
            track.add(new MidiEvent(noteOn, tick));

            // Note Off 事件
            ShortMessage noteOff = new ShortMessage();
            noteOff.setMessage(0x80, pitch, 0);
            track.add(new MidiEvent(noteOff, tick + ticksForDuration(duration)));
        } catch (InvalidMidiDataException e) {
            log.error("添加音符失败：pitch={}, velocity={}", pitch, velocity, e);
        }
    }

    /**
     * 将时长（秒）转换为 tick 数
     */
    private long ticksForDuration(double duration) {
        // BPM = 120, 所以每拍 = 0.5 秒
        // 每个 tick = 1/480 拍
        double beats = duration * (TEMPO / 60.0);
        return (long) (beats * TICKS_PER_BEAT);
    }

    /**
     * 生成和弦
     *
     * @param track 音轨
     * @param tick 时间位置
     * @param chord 和弦音符列表
     * @param velocity 音量
     * @param duration 时长
     */
    public void addChord(Track track, long tick, List<Integer> chord, int velocity, double duration) {
        for (int pitch : chord) {
            addNote(track, tick, pitch, velocity, duration);
        }
    }

    /**
     * 从 ComplexityMetrics 生成 MIDI 字节数组
     *
     * @param metrics 复杂度指标列表
     * @return MIDI 文件字节数组
     * @throws IOException IO 异常
     * @throws InvalidMidiDataException MIDI 数据异常
     */
    public byte[] generateMidiBytes(List<ComplexityMetrics> metrics)
            throws IOException, InvalidMidiDataException {

        log.info("开始生成 MIDI 字节数组");

        Sequence sequence = new Sequence(Sequence.PPQ, TICKS_PER_BEAT);
        var grouped = groupMetricsByInstrument(metrics);

        for (var entry : grouped.entrySet()) {
            Track track = sequence.createTrack();
            int instrument = entry.getKey();
            List<ComplexityMetrics> methodMetrics = entry.getValue();

            setInstrument(track, 0, instrument);

            long tickPosition = 0;
            for (ComplexityMetrics metric : methodMetrics) {
                int pitch = mapper.complexityToPitch(metric.getCyclomaticComplexity());
                int velocity = mapper.depthToVelocity(metric.getNestingDepth());
                double duration = mapper.linesToDuration(metric.getLinesOfCode());

                addNote(track, tickPosition, pitch, velocity, duration);
                tickPosition += ticksForDuration(duration);
            }
        }

        // 写入字节数组
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            MidiSystem.write(sequence, 1, baos);
            return baos.toByteArray();
        }
    }
}
