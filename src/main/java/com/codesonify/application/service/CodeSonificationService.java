package com.codesonify.application.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.service.MidiGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.sound.midi.InvalidMidiDataException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/**
 * 代码声音化应用服务
 *
 * 编排领域服务，提供代码声音化的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CodeSonificationService {

    private final MidiGenerator midiGenerator;

    /**
     * 将项目分析结果转换为 MIDI 音乐
     *
     * @param classes 类指标列表
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     * @throws InvalidMidiDataException MIDI 数据异常
     */
    public void sonifyProject(List<ClassMetrics> classes, String outputPath)
            throws IOException, InvalidMidiDataException {

        log.info("开始将项目代码转换为音乐");

        // 提取所有方法指标
        List<ComplexityMetrics> allMethods = classes.stream()
                .flatMap(c -> c.getMethods().stream())
                .toList();

        log.info("共分析 {} 个方法", allMethods.size());

        // 生成 MIDI 文件
        midiGenerator.generateMidi(allMethods, outputPath);

        log.info("项目声音化完成：{}", outputPath);
    }

    /**
     * 将单个类转换为 MIDI 音乐
     *
     * @param classMetrics 类指标
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     * @throws InvalidMidiDataException MIDI 数据异常
     */
    public void sonifyClass(ClassMetrics classMetrics, String outputPath)
            throws IOException, InvalidMidiDataException {

        log.info("开始将类 {} 转换为音乐", classMetrics.getClassName());

        // 生成 MIDI 文件
        midiGenerator.generateMidi(classMetrics.getMethods(), outputPath);

        log.info("类声音化完成：{}", outputPath);
    }

    /**
     * 生成 MIDI 字节数组
     *
     * @param classes 类指标列表
     * @return MIDI 文件字节数组
     * @throws IOException IO 异常
     * @throws InvalidMidiDataException MIDI 数据异常
     */
    public byte[] generateMidiBytes(List<ClassMetrics> classes)
            throws IOException, InvalidMidiDataException {

        List<ComplexityMetrics> allMethods = classes.stream()
                .flatMap(c -> c.getMethods().stream())
                .toList();

        return midiGenerator.generateMidiBytes(allMethods);
    }
}
