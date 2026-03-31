package com.codesonify.interfaces.dto;

import com.codesonify.domain.entity.RefactoringSuggestion;
import com.codesonify.domain.entity.TechnicalDebtScore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 技术债务分析响应 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TechnicalDebtAnalysisResponse {

    /**
     * 技术债务分数列表
     */
    private List<TechnicalDebtScoreDTO> scores;

    /**
     * 重构建议列表
     */
    private List<RefactoringSuggestionDTO> suggestions;

    /**
     * 统计信息
     */
    private TechnicalDebtStatisticsDTO statistics;

    /**
     * 热力图文件路径
     */
    private String heatmapPath;

    /**
     * 分析时间戳
     */
    private Long analysisTimestamp;

    /**
     * 技术债务分数 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalDebtScoreDTO {
        private String className;
        private String packageName;
        private String filePath;
        private double totalScore;
        private double complexityScore;
        private double couplingScore;
        private double locScore;
        private double nestingScore;
        private double methodCountScore;
        private String debtLevel;
        private String levelName;
        private String levelColor;
        private int estimatedFixHours;
        private Long analysisTimestamp;

        public static TechnicalDebtScoreDTO fromEntity(TechnicalDebtScore score) {
            return TechnicalDebtScoreDTO.builder()
                    .className(score.getClassName())
                    .packageName(score.getPackageName())
                    .filePath(score.getFilePath())
                    .totalScore(score.getTotalScore())
                    .complexityScore(score.getComplexityScore())
                    .couplingScore(score.getCouplingScore())
                    .locScore(score.getLocScore())
                    .nestingScore(score.getNestingScore())
                    .methodCountScore(score.getMethodCountScore())
                    .debtLevel(score.getDebtLevel().name())
                    .levelName(score.getLevelName())
                    .levelColor(score.getLevelColor())
                    .estimatedFixHours(score.getEstimatedFixHours())
                    .analysisTimestamp(score.getAnalysisTimestamp())
                    .build();
        }
    }

    /**
     * 重构建议 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RefactoringSuggestionDTO {
        private String type;
        private String typeName;
        private String priority;
        private String priorityName;
        private String priorityColor;
        private String className;
        private String packageName;
        private String methodName;
        private String description;
        private double currentValue;
        private double targetValue;
        private double impactScore;
        private String suggestion;
        private String codeExample;
        private Long analysisTimestamp;

        public static RefactoringSuggestionDTO fromEntity(RefactoringSuggestion suggestion) {
            return RefactoringSuggestionDTO.builder()
                    .type(suggestion.getType().name())
                    .typeName(suggestion.getTypeName())
                    .priority(suggestion.getPriority().name())
                    .priorityName(suggestion.getPriorityName())
                    .priorityColor(suggestion.getPriorityColor())
                    .className(suggestion.getClassName())
                    .packageName(suggestion.getPackageName())
                    .methodName(suggestion.getMethodName())
                    .description(suggestion.getDescription())
                    .currentValue(suggestion.getCurrentValue())
                    .targetValue(suggestion.getTargetValue())
                    .impactScore(suggestion.getImpactScore())
                    .suggestion(suggestion.getSuggestion())
                    .codeExample(suggestion.getCodeExample())
                    .analysisTimestamp(suggestion.getAnalysisTimestamp())
                    .build();
        }
    }

    /**
     * 技术债务统计信息 DTO
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TechnicalDebtStatisticsDTO {
        private int totalClasses;
        private int noneDebtCount;
        private int lowDebtCount;
        private int mediumDebtCount;
        private int highDebtCount;
        private int criticalDebtCount;
        private double averageScore;
        private double maxScore;
        private double minScore;
        private int totalEstimatedHours;
    }
}