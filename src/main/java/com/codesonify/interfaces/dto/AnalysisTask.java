package com.codesonify.interfaces.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 分析任务请求 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisTask implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务 ID
     */
    private String taskId;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 任务类型
     */
    private TaskType taskType;

    /**
     * 回调 URL（可选）
     */
    private String callbackUrl;

    /**
     * 任务创建时间戳
     */
    private long timestamp;

    /**
     * 任务类型枚举
     */
    public enum TaskType {
        /**
         * 分析整个项目
         */
        ANALYZE_PROJECT,

        /**
         * 分析单个文件
         */
        ANALYZE_FILE,

        /**
         * 生成报告
         */
        GENERATE_REPORT,

        /**
         * 声音化
         */
        SONIFY
    }
}
