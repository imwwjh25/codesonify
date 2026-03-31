package com.codesonify.interfaces.controller;

import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import com.codesonify.interfaces.dto.CodeSmellDTO;
import com.codesonify.interfaces.request.CodeSmellDetectionRequest;
import com.codesonify.interfaces.response.CodeSmellAnalysisResponse;
import com.codesonify.application.service.CodeSmellAnalysisService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 代码异味检测控制器
 *
 * 提供代码异味检测的 REST API
 */
@RestController
@RequestMapping("/api/codesmells")
@Tag(name = "代码异味检测", description = "代码异味检测相关 API")
@Slf4j
public class CodeSmellController {

    @Autowired
    private CodeSmellAnalysisService codeSmellAnalysisService;

    /**
     * 分析项目代码异味
     *
     * @param request 检测请求
     * @return 分析结果
     */
    @PostMapping("/analyze")
    @Operation(summary = "分析项目代码异味", description = "对指定项目进行代码异味检测，返回分析结果")
    public ResponseEntity<CodeSmellAnalysisResponse> analyzeCodeSmells(
            @Valid @RequestBody CodeSmellDetectionRequest request) {
        log.info("收到代码异味检测请求: {}", request.getProjectPath());

        try {
            CodeSmellAnalysisResponse response = codeSmellAnalysisService.analyzeProject(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("代码异味检测失败: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 获取分析结果
     *
     * @param analysisId 分析 ID
     * @return 分析结果
     */
    @GetMapping("/{analysisId}")
    @Operation(summary = "获取分析结果", description = "根据分析 ID 获取代码异味分析结果")
    public ResponseEntity<CodeSmellAnalysisResponse> getAnalysisResult(
            @Parameter(description = "分析 ID") @PathVariable String analysisId) {
        log.info("获取代码异味分析结果: {}", analysisId);

        CodeSmellAnalysisResponse response = codeSmellAnalysisService.getAnalysisResponse(analysisId);
        if (response == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有代码异味
     *
     * @param analysisId 分析 ID
     * @return 代码异味列表
     */
    @GetMapping("/{analysisId}/smells")
    @Operation(summary = "获取所有代码异味", description = "获取指定分析的所有代码异味")
    public ResponseEntity<List<CodeSmellDTO>> getCodeSmells(
            @Parameter(description = "分析 ID") @PathVariable String analysisId) {
        log.info("获取代码异味列表: {}", analysisId);

        List<CodeSmellDTO> codeSmells = codeSmellAnalysisService.getCodeSmells(analysisId);
        return ResponseEntity.ok(codeSmells);
    }

    /**
     * 按类型获取代码异味
     *
     * @param analysisId 分析 ID
     * @param type 异味类型
     * @return 代码异味列表
     */
    @GetMapping("/{analysisId}/smells/type/{type}")
    @Operation(summary = "按类型获取代码异味", description = "获取指定类型的代码异味")
    public ResponseEntity<List<CodeSmellDTO>> getCodeSmellsByType(
            @Parameter(description = "分析 ID") @PathVariable String analysisId,
            @Parameter(description = "异味类型") @PathVariable SmellType type) {
        log.info("获取代码异味列表: {} (类型: {})", analysisId, type);

        List<CodeSmellDTO> codeSmells = codeSmellAnalysisService.getCodeSmellsByType(analysisId, type);
        return ResponseEntity.ok(codeSmells);
    }

    /**
     * 按严重程度获取代码异味
     *
     * @param analysisId 分析 ID
     * @param severity 严重程度
     * @return 代码异味列表
     */
    @GetMapping("/{analysisId}/smells/severity/{severity}")
    @Operation(summary = "按严重程度获取代码异味", description = "获取指定严重程度的代码异味")
    public ResponseEntity<List<CodeSmellDTO>> getCodeSmellsBySeverity(
            @Parameter(description = "分析 ID") @PathVariable String analysisId,
            @Parameter(description = "严重程度") @PathVariable Severity severity) {
        log.info("获取代码异味列表: {} (严重程度: {})", analysisId, severity);

        List<CodeSmellDTO> codeSmells = codeSmellAnalysisService.getCodeSmellsBySeverity(analysisId, severity);
        return ResponseEntity.ok(codeSmells);
    }
}