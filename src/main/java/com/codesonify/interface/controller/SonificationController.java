package com.codesonify.interface.controller;

import com.codesonify.interface.dto.SonificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 声音化控制器
 *
 * 提供将代码复杂度转换为音乐的 HTTP 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/sonification")
@RequiredArgsConstructor
public class SonificationController {

    /**
     * 生成代码音乐
     *
     * @param analysisId 分析 ID
     * @return 音乐生成结果
     */
    @PostMapping("/generate")
    public ResponseEntity<SonificationResponse> generateMusic(
            @RequestParam String analysisId) {

        log.info("生成代码音乐，分析 ID: {}", analysisId);

        // TODO: 调用应用服务层生成音乐
        SonificationResponse response = SonificationResponse.builder()
                .success(true)
                .message("音乐生成成功")
                .audioFilePath("/tmp/output.mid")
                .format("MIDI")
                .description(SonificationResponse.MusicDescriptionDTO.builder()
                        .key("C 大调")
                        .tempo("120 BPM")
                        .totalNotes(100)
                        .structure("A-B-A-C")
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 播放代码音乐
     *
     * @param analysisId 分析 ID
     * @return 音频文件
     */
    @GetMapping("/play/{analysisId}")
    public ResponseEntity<SonificationResponse> playMusic(
            @PathVariable String analysisId) {

        log.info("播放代码音乐，分析 ID: {}", analysisId);

        // TODO: 流式传输音频文件
        SonificationResponse response = SonificationResponse.builder()
                .success(true)
                .audioUrl("/api/sonification/stream/" + analysisId)
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 下载音频文件
     *
     * @param analysisId 分析 ID
     * @param format     音频格式（mp3, wav）
     * @return 音频文件
     */
    @GetMapping("/download/{analysisId}")
    public ResponseEntity<byte[]> downloadAudio(
            @PathVariable String analysisId,
            @RequestParam(defaultValue = "wav") String format) {

        log.info("下载音频文件，分析 ID: {}, 格式：{}", analysisId, format);

        // TODO: 生成并返回音频文件
        return ResponseEntity.ok().build();
    }
}
