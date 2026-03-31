package com.codesonify.interfaces.controller;

import com.codesonify.application.service.ReportGenerationService;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.repository.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * 报告生成控制器
 *
 * 提供 HTML 报告和依赖图生成的 HTTP 接口
 */
@Tag(name = "报告生成 API", description = "提供 HTML 报告和依赖图生成的 HTTP 接口")
@Slf4j
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
public class ReportController {

    private final ReportGenerationService reportGenerationService;
    private final CacheService cacheService;

    /**
     * 生成完整报告（HTML + Draw.io）
     *
     * @param analysisId 分析 ID
     * @return 报告生成结果
     */
    @Operation(summary = "生成完整报告", description = "生成 HTML 报告和 Draw.io 依赖图")
    @PostMapping("/generate")
    public ResponseEntity<ReportResponse> generateFullReport(
            @Parameter(description = "分析 ID", required = true)
            @RequestParam String analysisId) {
        try {
            log.info("生成完整报告，分析 ID: {}", analysisId);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body(ReportResponse.builder()
                        .success(false)
                        .error("分析结果不存在：" + analysisId)
                        .build());
            }

            // 生成输出目录
            String outputDir = System.getProperty("java.io.tmpdir") + "/codesonify/reports/" + UUID.randomUUID();
            new File(outputDir).mkdirs();

            // 生成完整报告
            reportGenerationService.generateFullReport(analysis, outputDir, null);

            ReportResponse response = ReportResponse.builder()
                    .success(true)
                    .message("报告生成成功")
                    .htmlReportPath(outputDir + "/report.html")
                    .drawioDiagramPath(outputDir + "/dependencies.drawio")
                    .reportId(UUID.randomUUID().toString())
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("报告生成失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(ReportResponse.builder()
                    .success(false)
                    .error("生成失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 生成 HTML 报告
     *
     * @param analysisId 分析 ID
     * @param includeMusic 是否包含 MIDI 播放器
     * @return HTML 文件
     */
    @Operation(summary = "生成 HTML 报告", description = "生成 HTML 格式的代码复杂度报告")
    @GetMapping("/html")
    public ResponseEntity<String> generateHtmlReport(
            @Parameter(description = "分析 ID", required = true)
            @RequestParam String analysisId,
            @Parameter(description = "是否包含 MIDI 播放器")
            @RequestParam(defaultValue = "false") boolean includeMusic) {
        try {
            log.info("生成 HTML 报告，分析 ID: {}, 包含音乐：{}", analysisId, includeMusic);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body("<html><body>分析结果不存在</body></html>");
            }

            // 生成临时文件
            String tempDir = System.getProperty("java.io.tmpdir") + "/codesonify";
            new File(tempDir).mkdirs();
            String outputPath = tempDir + "/report-" + UUID.randomUUID() + ".html";

            // 准备 MIDI Base64（如果需要）
            String midiBase64 = null;
            if (includeMusic) {
                // TODO: 生成 MIDI 并转换为 Base64
            }

            // 生成 HTML 报告（注意：ReportGenerationService.generateHtmlReport 只有两个参数）
            reportGenerationService.generateHtmlReport(analysis, outputPath);

            // 读取 HTML 内容
            String htmlContent = Files.readString(Path.of(outputPath));

            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(htmlContent);

        } catch (Exception e) {
            log.error("HTML 报告生成失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body("<html><body>报告生成失败：" + e.getMessage() + "</body></html>");
        }
    }

    /**
     * 生成 Draw.io 依赖图
     *
     * @param analysisId 分析 ID
     * @return Draw.io 文件
     */
    @Operation(summary = "生成依赖图", description = "生成 Draw.io 格式的依赖关系图")
    @GetMapping("/diagram")
    public ResponseEntity<String> generateDrawioDiagram(
            @Parameter(description = "分析 ID", required = true)
            @RequestParam String analysisId) {
        try {
            log.info("生成 Draw.io 依赖图，分析 ID: {}", analysisId);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body("分析结果不存在：" + analysisId);
            }

            // 生成临时文件
            String tempDir = System.getProperty("java.io.tmpdir") + "/codesonify";
            new File(tempDir).mkdirs();
            String outputPath = tempDir + "/dependencies-" + UUID.randomUUID() + ".drawio";

            // 生成 Draw.io 图表
            reportGenerationService.generateDrawioDiagram(analysis, outputPath);

            // 读取文件内容
            String content = Files.readString(Path.of(outputPath));

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_XML)
                    .body(content);

        } catch (Exception e) {
            log.error("Draw.io 图表生成失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body("图表生成失败：" + e.getMessage());
        }
    }

    /**
     * 下载报告包（ZIP）
     *
     * @param reportId 报告 ID
     * @return ZIP 文件
     */
    @Operation(summary = "下载报告", description = "下载报告包（ZIP 格式）")
    @GetMapping("/download/{reportId}")
    public ResponseEntity<Resource> downloadReport(
            @Parameter(description = "报告 ID", required = true)
            @PathVariable String reportId) {
        try {
            log.info("下载报告，报告 ID: {}", reportId);

            // TODO: 实现报告打包下载功能
            // 目前返回占位响应

            return ResponseEntity.ok().build();

        } catch (Exception e) {
            log.error("下载报告失败，报告 ID: {}", reportId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 报告响应 DTO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class ReportResponse {
        private boolean success;
        private String message;
        private String error;
        private String reportId;
        private String htmlReportPath;
        private String drawioDiagramPath;
    }
}
