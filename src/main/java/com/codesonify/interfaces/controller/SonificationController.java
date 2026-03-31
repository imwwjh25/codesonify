package com.codesonify.interfaces.controller;

import com.codesonify.application.service.CodeSonificationService;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.interfaces.dto.SonificationResponse;
import com.codesonify.repository.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

/**
 * 声音化控制器
 *
 * 提供将代码复杂度转换为音乐的 HTTP 接口
 */
@Tag(name = "声音化 API", description = "提供将代码复杂度转换为音乐的 HTTP 接口")
@Slf4j
@RestController
@RequestMapping("/api/sonification")
@RequiredArgsConstructor
public class SonificationController {

    private final CodeSonificationService codeSonificationService;
    private final CacheService cacheService;

    /**
     * 生成代码音乐
     *
     * @param analysisId 分析 ID
     */
    @Operation(summary = "生成代码音乐", description = "将代码复杂度转换为 MIDI 音乐")
    @PostMapping("/generate")
    public ResponseEntity<SonificationResponse> generateMusic(
            @Parameter(description = "分析 ID", required = true)
            @RequestParam String analysisId) {
        try {
            log.info("生成代码音乐，分析 ID: {}", analysisId);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body(SonificationResponse.builder()
                        .success(false)
                        .error("分析结果不存在：" + analysisId)
                        .build());
            }

            // 生成临时文件路径
            String tempDir = System.getProperty("java.io.tmpdir") + "/codesonify";
            new File(tempDir).mkdirs();
            String outputPath = tempDir + "/" + UUID.randomUUID() + ".mid";

            // 生成 MIDI 文件
            codeSonificationService.sonifyProject(analysis.getClasses(), outputPath);

            // 读取 MIDI 文件并转换为 Base64
            byte[] midiBytes = Files.readAllBytes(Path.of(outputPath));
            String midiBase64 = Base64.getEncoder().encodeToString(midiBytes);

            SonificationResponse response = SonificationResponse.builder()
                    .success(true)
                    .message("音乐生成成功")
                    .audioFilePath(outputPath)
                    .format("MIDI")
                    .duration(calculateDuration(analysis))
                    .description(SonificationResponse.MusicDescriptionDTO.builder()
                            .key("C 大调")
                            .tempo("120 BPM")
                            .totalNotes(countTotalNotes(analysis))
                            .structure("A-B-A-C")
                            .build())
                    .build();

            // 将 MIDI Base64 添加到响应头，方便前端使用
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-MIDI-Data", midiBase64);

            return ResponseEntity.ok().headers(headers).body(response);

        } catch (Exception e) {
            log.error("音乐生成失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(SonificationResponse.builder()
                    .success(false)
                    .error("生成失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 生成代码音乐并返回 Base64
     *
     * @param analysisId 分析 ID
     * @return Base64 编码的 MIDI 数据
     */
    @Operation(summary = "生成代码音乐（Base64）", description = "将代码复杂度转换为 MIDI 并返回 Base64 编码")
    @GetMapping("/generate/base64")
    public ResponseEntity<SonificationResponse> generateMusicBase64(
            @Parameter(description = "分析 ID", required = true)
            @RequestParam String analysisId) {
        try {
            log.info("生成代码音乐（Base64），分析 ID: {}", analysisId);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body(SonificationResponse.builder()
                        .success(false)
                        .error("分析结果不存在：" + analysisId)
                        .build());
            }

            // 生成 MIDI 字节数组
            byte[] midiBytes = codeSonificationService.generateMidiBytes(analysis.getClasses());
            String midiBase64 = Base64.getEncoder().encodeToString(midiBytes);

            SonificationResponse response = SonificationResponse.builder()
                    .success(true)
                    .message("音乐生成成功")
                    .format("MIDI")
                    .duration(calculateDuration(analysis))
                    .build();

            return ResponseEntity.ok()
                    .header("X-MIDI-Base64", midiBase64)
                    .body(response);

        } catch (Exception e) {
            log.error("音乐生成失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(SonificationResponse.builder()
                    .success(false)
                    .error("生成失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 播放代码音乐（返回音频 URL）
     *
     * @param analysisId 分析 ID
     * @return 音频文件 URL
     */
    @Operation(summary = "播放代码音乐", description = "获取代码音乐的播放 URL")
    @GetMapping("/play/{analysisId}")
    public ResponseEntity<SonificationResponse> playMusic(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId) {
        try {
            log.info("播放代码音乐，分析 ID: {}", analysisId);

            // 检查分析结果是否存在
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body(SonificationResponse.builder()
                        .success(false)
                        .error("分析结果不存在：" + analysisId)
                        .build());
            }

            // 返回音频流 URL
            String audioUrl = "/api/sonification/stream/" + analysisId;

            SonificationResponse response = SonificationResponse.builder()
                    .success(true)
                    .audioUrl(audioUrl)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("播放音乐失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(SonificationResponse.builder()
                    .success(false)
                    .error("播放失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 流式传输音频文件
     *
     * @param analysisId 分析 ID
     * @return 音频文件字节流
     */
    @Operation(summary = "流式传输音频", description = "流式传输代码音乐音频文件")
    @GetMapping("/stream/{analysisId}")
    public ResponseEntity<byte[]> streamAudio(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId) {
        try {
            log.info("流式传输音频，分析 ID: {}", analysisId);

            // TODO: 从存储中读取音频文件
            // 目前返回空响应
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("流式传输失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 下载音频文件
     *
     * @param analysisId 分析 ID
     * @param format     音频格式（midi, wav）
     * @return 音频文件
     */
    @Operation(summary = "下载音频文件", description = "下载代码音乐音频文件")
    @GetMapping("/download/{analysisId}")
    public ResponseEntity<byte[]> downloadAudio(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId,
            @Parameter(description = "音频格式")
            @RequestParam(defaultValue = "midi") String format) {
        try {
            log.info("下载音频文件，分析 ID: {}, 格式：{}", analysisId, format);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().build();
            }

            // 生成 MIDI 字节数组
            byte[] midiBytes = codeSonificationService.generateMidiBytes(analysis.getClasses());

            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType("audio/" + format));
            headers.setContentDispositionFormData("attachment", "codesonify-" + analysisId + "." + format);

            return ResponseEntity.ok()
                    .headers(headers)
                    .body(midiBytes);

        } catch (Exception e) {
            log.error("下载音频失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 计算音乐时长（估算）
     */
    private double calculateDuration(ProjectAnalysis analysis) {
        int totalMethods = analysis.getStatistics().getTotalMethods();
        // 假设每个方法对应 2 秒音乐
        return totalMethods * 2.0;
    }

    /**
     * 计算总音符数（估算）
     */
    private int countTotalNotes(ProjectAnalysis analysis) {
        int totalMethods = analysis.getStatistics().getTotalMethods();
        // 假设每个方法对应 8 个音符
        return totalMethods * 8;
    }
}
