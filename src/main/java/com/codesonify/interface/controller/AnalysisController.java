package com.codesonify.interface.controller;

import com.codesonify.interface.dto.AnalysisResponse;
import com.codesonify.interface.dto.SonificationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

/**
 * 代码分析控制器
 *
 * 提供代码复杂度分析的 HTTP 接口
 */
@Slf4j
@RestController
@RequestMapping("/api/analysis")
@RequiredArgsConstructor
public class AnalysisController {

    /**
     * 分析项目代码
     *
     * @param projectPath 项目路径
     * @return 分析结果
     */
    @PostMapping("/analyze")
    public ResponseEntity<AnalysisResponse> analyzeProject(
            @RequestParam String projectPath) {

        log.info("开始分析项目：{}", projectPath);

        // TODO: 调用应用服务层进行分析
        // 目前返回示例响应

        AnalysisResponse response = AnalysisResponse.builder()
                .success(true)
                .message("分析成功")
                .projectAnalysis(AnalysisResponse.ProjectAnalysisDTO.builder()
                        .projectName("示例项目")
                        .projectPath(projectPath)
                        .analysisTime(java.time.LocalDateTime.now().toString())
                        .statistics(AnalysisResponse.StatisticsDTO.builder()
                                .totalClasses(10)
                                .totalMethods(50)
                                .averageCyclomaticComplexity(5.5)
                                .maxCyclomaticComplexity(15)
                                .totalLinesOfCode(2000)
                                .complexityDistribution(AnalysisResponse.ComplexityDistributionDTO.builder()
                                        .simple(30)
                                        .moderate(15)
                                        .complex(5)
                                        .veryComplex(0)
                                        .build())
                                .build())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 分析单个文件
     *
     * @param filePath 文件路径
     * @return 分析结果
     */
    @PostMapping("/analyze-file")
    public ResponseEntity<AnalysisResponse> analyzeFile(
            @RequestParam String filePath) {

        log.info("开始分析文件：{}", filePath);

        File file = new File(filePath);
        if (!file.exists()) {
            AnalysisResponse errorResponse = AnalysisResponse.builder()
                    .success(false)
                    .error("文件不存在：" + filePath)
                    .build();
            return ResponseEntity.badRequest().body(errorResponse);
        }

        // TODO: 调用应用服务层进行分析
        AnalysisResponse response = AnalysisResponse.builder()
                .success(true)
                .message("文件分析成功")
                .build();

        return ResponseEntity.ok(response);
    }

    /**
     * 获取分析结果
     *
     * @param analysisId 分析 ID
     * @return 分析结果
     */
    @GetMapping("/{analysisId}")
    public ResponseEntity<AnalysisResponse> getAnalysisResult(
            @PathVariable String analysisId) {

        log.info("获取分析结果：{}", analysisId);

        // TODO: 从 Redis 或数据库获取分析结果
        AnalysisResponse response = AnalysisResponse.builder()
                .success(true)
                .message("获取成功")
                .build();

        return ResponseEntity.ok(response);
    }
}
