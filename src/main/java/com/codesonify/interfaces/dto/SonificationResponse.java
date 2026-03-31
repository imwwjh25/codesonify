package com.codesonify.interfaces.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 音乐生成响应 DTO
 */
@Data
@Builder
public class SonificationResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 音频文件路径
     */
    private String audioFilePath;

    /**
     * 音频文件 URL（用于 Web 播放）
     */
    private String audioUrl;

    /**
     * 音频时长（秒）
     */
    private double duration;

    /**
     * 音频格式
     */
    private String format;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 音乐描述
     */
    private MusicDescriptionDTO description;

    /**
     * 音乐描述 DTO
     */
    @Data
    @Builder
    public static class MusicDescriptionDTO {
        private String key;          // 调性
        private String tempo;        // 速度
        private int totalNotes;      // 总音符数
        private String structure;    // 音乐结构
    }
}
