package com.codesonify.interfaces.dto;

import com.codesonify.domain.entity.ClassMetrics;
import lombok.Data;

import java.util.List;

/**
 * 技术债务分析请求 DTO
 */
@Data
public class TechnicalDebtAnalysisRequest {

    /**
     * 项目路径
     */
    private String projectPath;

    /**
     * 是否生成热力图
     */
    private Boolean generateHeatmap = true;

    /**
     * 是否生成重构建议
     */
    private Boolean generateSuggestions = true;

    /**
     * 输出目录
     */
    private String outputDir = "./output/technical-debt";

    /**
     * 类复杂度指标列表（如果直接提供分析结果）
     */
    private List<ClassMetrics> classMetrics;
}