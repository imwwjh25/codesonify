package com.codesonify.application.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.CodeSmell;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.domain.service.CodeSmellAggregator;
import com.codesonify.domain.service.CodeSmellAggregator.CodeSmellStatistics;
import com.codesonify.domain.service.CodeSmellDetector;
import com.codesonify.domain.service.DuplicateCodeAnalyzer;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import com.codesonify.interfaces.dto.CodeSmellDTO;
import com.codesonify.interfaces.request.CodeSmellDetectionRequest;
import com.codesonify.interfaces.response.CodeSmellAnalysisResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 代码异味分析应用服务
 *
 * 编排代码异味检测流程
 */
@Service
@Slf4j
public class CodeSmellAnalysisService {

    @Autowired
    private CodeAnalysisService codeAnalysisService;

    @Autowired
    private CodeSmellDetector codeSmellDetector;

    @Autowired
    private DuplicateCodeAnalyzer duplicateCodeAnalyzer;

    @Autowired
    private CodeSmellAggregator codeSmellAggregator;

    /**
     * 分析结果缓存
     */
    private final Map<String, AnalysisCacheEntry> analysisCache = new HashMap<>();

    /**
     * 分析项目代码异味
     *
     * @param request 检测请求
     * @return 分析结果
     */
    public CodeSmellAnalysisResponse analyzeProject(CodeSmellDetectionRequest request) throws IOException {
        log.info("开始分析项目代码异味: {}", request.getProjectPath());

        // 设置检测器阈值
        applyThresholds(request);

        // 执行代码分析
        ProjectAnalysis analysisResult = codeAnalysisService.analyzeProject(request.getProjectPath());

        // 检测代码异味
        List<CodeSmell> codeSmells = codeSmellDetector.detectCodeSmells(analysisResult.getClasses());

        // 检测重复代码（如果启用）
        if (request.isDetectDuplicateCode()) {
            List<CodeSmell> duplicateSmells = duplicateCodeAnalyzer.convertToCodeSmells(
                    duplicateCodeAnalyzer.analyzeDuplicates(analysisResult.getClasses())
            );
            codeSmells.addAll(duplicateSmells);
        }

        // 生成统计信息
        CodeSmellStatistics statistics = codeSmellAggregator.generateStatistics(codeSmells);

        // 构建响应
        String analysisId = UUID.randomUUID().toString();

        CodeSmellAnalysisResponse response = CodeSmellAnalysisResponse.builder()
                .analysisId(analysisId)
                .projectName(request.getProjectName() != null ? request.getProjectName() : extractProjectName(request.getProjectPath()))
                .projectPath(request.getProjectPath())
                .analysisTimestamp(System.currentTimeMillis())
                .totalCount(statistics.getTotalCount())
                .severityStats(CodeSmellAnalysisResponse.SeverityStats.builder()
                        .critical(statistics.getCriticalCount())
                        .high(statistics.getHighCount())
                        .medium(statistics.getMediumCount())
                        .low(statistics.getLowCount())
                        .build())
                .typeDistribution(convertTypeDistribution(statistics.getTypeDistribution()))
                .smellScore(statistics.getAverageScore())
                .qualityScore(codeSmellAggregator.calculateQualityScore(codeSmells))
                .qualityLevel(codeSmellAggregator.getQualityLevel(codeSmells))
                .topAffectedClasses(statistics.getTopAffectedClasses())
                .build();

        // 缓存分析结果
        cacheAnalysisResult(analysisId, codeSmells, response);

        log.info("代码异味分析完成: 发现 {} 个异味, 评分: {}",
                codeSmells.size(), response.getSmellScore());

        return response;
    }

    /**
     * 获取分析结果的代码异味列表
     *
     * @param analysisId 分析 ID
     * @return 代码异味列表
     */
    public List<CodeSmellDTO> getCodeSmells(String analysisId) {
        AnalysisCacheEntry entry = analysisCache.get(analysisId);
        if (entry == null) {
            return Collections.emptyList();
        }

        return entry.getCodeSmells().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按类型筛选代码异味
     *
     * @param analysisId 分析 ID
     * @param type 类型
     * @return 代码异味列表
     */
    public List<CodeSmellDTO> getCodeSmellsByType(String analysisId, SmellType type) {
        AnalysisCacheEntry entry = analysisCache.get(analysisId);
        if (entry == null) {
            return Collections.emptyList();
        }

        return codeSmellAggregator.filterByType(entry.getCodeSmells(), type).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 按严重程度筛选代码异味
     *
     * @param analysisId 分析 ID
     * @param severity 严重程度
     * @return 代码异味列表
     */
    public List<CodeSmellDTO> getCodeSmellsBySeverity(String analysisId, Severity severity) {
        AnalysisCacheEntry entry = analysisCache.get(analysisId);
        if (entry == null) {
            return Collections.emptyList();
        }

        return codeSmellAggregator.filterBySeverity(entry.getCodeSmells(), severity).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 获取分析响应
     *
     * @param analysisId 分析 ID
     * @return 分析响应
     */
    public CodeSmellAnalysisResponse getAnalysisResponse(String analysisId) {
        AnalysisCacheEntry entry = analysisCache.get(analysisId);
        return entry != null ? entry.getResponse() : null;
    }

    /**
     * 应用阈值配置
     *
     * @param request 检测请求
     */
    private void applyThresholds(CodeSmellDetectionRequest request) {
        if (request.getLongMethodMaxLines() != null) {
            codeSmellDetector.setLongMethodMaxLines(request.getLongMethodMaxLines());
        }
        if (request.getLongMethodMaxNestingDepth() != null) {
            codeSmellDetector.setLongMethodMaxNestingDepth(request.getLongMethodMaxNestingDepth());
        }
        if (request.getLongMethodMaxComplexity() != null) {
            codeSmellDetector.setLongMethodMaxComplexity(request.getLongMethodMaxComplexity());
        }
        if (request.getLargeClassMaxLines() != null) {
            codeSmellDetector.setLargeClassMaxLines(request.getLargeClassMaxLines());
        }
        if (request.getLargeClassMaxMethods() != null) {
            codeSmellDetector.setLargeClassMaxMethods(request.getLargeClassMaxMethods());
        }
        if (request.getLargeClassMaxFields() != null) {
            codeSmellDetector.setLargeClassMaxFields(request.getLargeClassMaxFields());
        }
        if (request.getLongParameterListMaxParams() != null) {
            codeSmellDetector.setLongParameterListMaxParams(request.getLongParameterListMaxParams());
        }
        if (request.getHighCouplingMaxCBO() != null) {
            codeSmellDetector.setHighCouplingMaxCBO(request.getHighCouplingMaxCBO());
        }
    }

    /**
     * 转换类型分布
     *
     * @param typeDistribution 类型分布
     * @return Map 格式的类型分布
     */
    private Map<String, Integer> convertTypeDistribution(Map<SmellType, Integer> typeDistribution) {
        return typeDistribution.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().getDisplayName(),
                        Map.Entry::getValue
                ));
    }

    /**
     * 提取项目名称
     *
     * @param projectPath 项目路径
     * @return 项目名称
     */
    private String extractProjectName(String projectPath) {
        String[] parts = projectPath.split("[/\\\\]");
        return parts[parts.length - 1];
    }

    /**
     * 转换为 DTO
     *
     * @param codeSmell 代码异味实体
     * @return DTO
     */
    private CodeSmellDTO convertToDTO(CodeSmell codeSmell) {
        return CodeSmellDTO.builder()
                .id(codeSmell.getId().toString())
                .type(codeSmell.getType())
                .severity(codeSmell.getSeverity())
                .className(codeSmell.getClassName())
                .methodName(codeSmell.getMethodName())
                .lineNumber(codeSmell.getLineNumber())
                .message(codeSmell.getMessage())
                .suggestion(codeSmell.getSuggestion())
                .metricValue(codeSmell.getMetricValue())
                .threshold(codeSmell.getThreshold())
                .build();
    }

    /**
     * 缓存分析结果
     *
     * @param analysisId 分析 ID
     * @param codeSmells 代码异味列表
     * @param response 响应
     */
    private void cacheAnalysisResult(String analysisId, List<CodeSmell> codeSmells, CodeSmellAnalysisResponse response) {
        AnalysisCacheEntry entry = new AnalysisCacheEntry();
        entry.setCodeSmells(codeSmells);
        entry.setResponse(response);
        entry.setTimestamp(System.currentTimeMillis());

        analysisCache.put(analysisId, entry);

        // 限制缓存大小
        if (analysisCache.size() > 100) {
            removeOldestEntry();
        }
    }

    /**
     * 移除最旧的缓存条目
     */
    private void removeOldestEntry() {
        String oldestKey = analysisCache.entrySet().stream()
                .min(Map.Entry.comparingByValue(Comparator.comparingLong(AnalysisCacheEntry::getTimestamp)))
                .map(Map.Entry::getKey)
                .orElse(null);

        if (oldestKey != null) {
            analysisCache.remove(oldestKey);
        }
    }

    /**
     * 分析缓存条目
     */
    @lombok.Data
    private static class AnalysisCacheEntry {
        private List<CodeSmell> codeSmells;
        private CodeSmellAnalysisResponse response;
        private long timestamp;
    }
}