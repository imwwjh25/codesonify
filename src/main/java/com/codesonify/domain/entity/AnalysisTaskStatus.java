package com.codesonify.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 分析任务状态实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTaskStatus {

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 任务状态
     */
    private TaskStatus status;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 分析结果 ID（完成后填充）
     */
    private String analysisId;

    /**
     * 错误信息（失败时填充）
     */
    private String errorMessage;

    /**
     * 任务创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 任务完成时间
     */
    private LocalDateTime completedAt;

    /**
     * 任务状态枚举
     */
    public enum TaskStatus {
        /**
         * 等待中
         */
        PENDING,

        /**
         * 处理中
         */
        PROCESSING,

        /**
         * 已完成
         */
        COMPLETED,

        /**
         * 失败
         */
        FAILED
    }
}
