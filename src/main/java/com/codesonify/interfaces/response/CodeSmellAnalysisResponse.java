package com.codesonify.interfaces.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * 代码异味分析结果响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodeSmellAnalysisResponse {
    /**
     * 分析 ID
     */
    private String analysisId;

    /**
     * 项目名称
     */
    private String projectName;

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;

    /**
     * 代码异味总数
     */
    private int totalCount;

    /**
     * 严重程度统计
     */
    private SeverityStats severityStats;

    /**
     * 类型分布
     */
    private Map<String, Integer> typeDistribution;

    /**
     * 代码异味评分 (0-100, 越高越差)
     */
    private double smellScore;

    /**
     * 代码质量评分 (0-100, 越高越好)
     */
    private double qualityScore;

    /**
     * 质量等级
     */
    private String qualityLevel;

    /**
     * 最受影响的类
     */
    private List<String> topAffectedClasses;

    /**
     * 严重程度统计详情
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SeverityStats {
        private int critical;
        private int high;
        private int medium;
        private int low;
    }
}