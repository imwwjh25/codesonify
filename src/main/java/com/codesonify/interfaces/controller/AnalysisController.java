package com.codesonify.interfaces.controller;

import com.codesonify.application.service.CodeAnalysisService;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.interfaces.dto.AnalysisResponse;
import com.codesonify.repository.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.UUID;

/**
 * 代码分析控制器
 *
 * 提供代码复杂度分析的 HTTP 接口
 */
@Slf4j
@Tag(name = "代码分析 API", description = "提供代码复杂度分析的 HTTP 接口")
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    private final CodeAnalysisService codeAnalysisService;
    private final CacheService cacheService;

    /**
     * 分析项目代码（同步）
     *
     * @param projectPath 项目路径
     * @return 分析结果
     */
    @Operation(summary = "分析项目代码（同步）", description = "同步分析整个项目的代码复杂度")
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeProject(
            @Parameter(description = "项目路径", required = true)
            @RequestParam String projectPath) {
        try {
            log.info("开始同步分析项目：{}", projectPath);

            File projectDir = new File(projectPath);
            if (!projectDir.exists()) {
                return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                        .success(false)
                        .error("项目路径不存在：" + projectPath)
                        .build());
            }

            // 执行分析
            ProjectAnalysis analysis = codeAnalysisService.analyzeProject(projectPath);

            // 生成分析 ID 并缓存结果
            String analysisId = UUID.randomUUID().toString();
            cacheService.cacheAnalysisResult(analysisId, analysis);

            AnalysisResponse response = toAnalysisResponse(analysis, analysisId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("项目分析失败：{}", projectPath, e);
            return ResponseEntity.internalServerError().body(AnalysisResponse.builder()
                    .success(false)
                    .error("分析失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 分析单个文件
     *
     * @param filePath 文件路径
     * @return 分析结果
     */
    @Operation(summary = "分析单个文件", description = "分析单个 Java 文件的代码复杂度")
    @PostMapping("/analyze-file")
    public ResponseEntity<AnalysisResponse> analyzeFile(
            @Parameter(description = "文件路径", required = true)
            @RequestParam String filePath) {
        try {
            log.info("开始分析文件：{}", filePath);

            File file = new File(filePath);
            if (!file.exists()) {
                return ResponseEntity.badRequest().body(AnalysisResponse.builder()
                        .success(false)
                        .error("文件不存在：" + filePath)
                        .build());
            }

            // 执行分析
            var classMetrics = codeAnalysisService.analyzeFile(filePath);

            // 生成分析 ID
            String analysisId = UUID.randomUUID().toString();

            AnalysisResponse response = AnalysisResponse.builder()
                    .success(true)
                    .message("文件分析成功")
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("文件分析失败：{}", filePath, e);
            return ResponseEntity.internalServerError().body(AnalysisResponse.builder()
                    .success(false)
                    .error("分析失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 获取分析结果
     *
     * @param analysisId 分析 ID
     * @return 分析结果
     */
    @Operation(summary = "获取分析结果", description = "从缓存中获取分析结果")
    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResponse> getAnalysisResult(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId) {
        try {
            log.info("获取分析结果：{}", analysisId);

            // 首先尝试从缓存获取
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis != null) {
                AnalysisResponse response = toAnalysisResponse(analysis, analysisId);
                return ResponseEntity.ok(response);
            }

            // 缓存未命中，返回 404
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            log.error("获取分析结果失败：{}", analysisId, e);
            return ResponseEntity.internalServerError().body(AnalysisResponse.builder()
                    .success(false)
                    .error("获取失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 将 ProjectAnalysis 转换为 AnalysisResponse
     */
    private AnalysisResponse toAnalysisResponse(ProjectAnalysis analysis, String analysisId) {
        return AnalysisResponse.builder()
                .success(true)
                .message("分析成功")
                .projectAnalysis(AnalysisResponse.ProjectAnalysisDTO.builder()
                        .projectName(analysis.getProjectName())
                        .projectPath(analysis.getProjectPath())
                        .analysisTime(analysis.getAnalysisTime().toString())
                        .statistics(AnalysisResponse.StatisticsDTO.builder()
                                .totalClasses(analysis.getStatistics().getTotalClasses())
                                .totalMethods(analysis.getStatistics().getTotalMethods())
                                .averageCyclomaticComplexity(analysis.getStatistics().getAverageCyclomaticComplexity())
                                .maxCyclomaticComplexity(analysis.getStatistics().getMaxCyclomaticComplexity())
                                .totalLinesOfCode(analysis.getStatistics().getTotalLinesOfCode())
                                .complexityDistribution(AnalysisResponse.ComplexityDistributionDTO.builder()
                                        .simple(analysis.getStatistics().getComplexityDistribution().getSimple())
                                        .moderate(analysis.getStatistics().getComplexityDistribution().getModerate())
                                        .complex(analysis.getStatistics().getComplexityDistribution().getComplex())
                                        .veryComplex(analysis.getStatistics().getComplexityDistribution().getVeryComplex())
                                        .build())
                                .build())
                        .build())
                .build();
    }
}
