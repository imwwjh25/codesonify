package com.codesonify.interfaces.controller;

import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.domain.service.ThreeDVisualizationService;
import com.codesonify.interfaces.dto.ThreeDVisualizationResponse;
import com.codesonify.repository.CacheService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 3D 可视化控制器
 *
 * 提供 3D 场景数据的 HTTP 接口
 */
@Tag(name = "3D 可视化 API", description = "提供 3D 场景数据的 HTTP 接口")
@Slf4j
@RestController
@RequestMapping("/api/visualization")
@RequiredArgsConstructor
public class ThreeDVisualizationController {

    private final ThreeDVisualizationService threeDVisualizationService;
    private final CacheService cacheService;

    /**
     * 获取 3D 场景数据
     *
     * @param analysisId 分析 ID
     * @return 3D 场景数据
     */
    @Operation(summary = "获取 3D 场景数据", description = "根据分析 ID 获取 3D 可视化场景数据")
    @GetMapping("/3d/{analysisId}")
    public ResponseEntity<ThreeDVisualizationResponse> getThreeDData(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId) {
        try {
            log.info("获取 3D 场景数据，分析 ID: {}", analysisId);

            // 从缓存获取分析结果
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.badRequest().body(ThreeDVisualizationResponse.builder()
                        .success(false)
                        .error("分析结果不存在：" + analysisId)
                        .build());
            }

            // 生成 3D 场景数据
            ThreeDVisualizationResponse.SceneData sceneData =
                threeDVisualizationService.generateSceneData(analysis);

            // 更新统计信息（包含连线数）
            ThreeDVisualizationResponse.Statistics stats = sceneData.getStatistics();
            if (stats != null) {
                ThreeDVisualizationResponse.Statistics updatedStats =
                    ThreeDVisualizationResponse.Statistics.builder()
                        .totalNodes(stats.getTotalNodes())
                        .totalEdges(sceneData.getEdges() != null ? sceneData.getEdges().size() : 0)
                        .maxHeight(stats.getMaxHeight())
                        .minHeight(stats.getMinHeight())
                        .build();
                sceneData = ThreeDVisualizationResponse.SceneData.builder()
                    .nodes(sceneData.getNodes())
                    .edges(sceneData.getEdges())
                    .statistics(updatedStats)
                    .build();
            }

            ThreeDVisualizationResponse response = ThreeDVisualizationResponse.builder()
                    .success(true)
                    .message("3D 场景数据生成成功")
                    .sceneData(sceneData)
                    .build();

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("获取 3D 场景数据失败，分析 ID: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(ThreeDVisualizationResponse.builder()
                    .success(false)
                    .error("获取失败：" + e.getMessage())
                    .build());
        }
    }

    /**
     * 验证分析 ID 是否存在
     *
     * @param analysisId 分析 ID
     * @return 验证结果
     */
    @Operation(summary = "验证分析 ID", description = "验证分析 ID 是否存在并可用于 3D 可视化")
    @GetMapping("/validate/{analysisId}")
    public ResponseEntity<ThreeDVisualizationResponse> validateAnalysisId(
            @Parameter(description = "分析 ID", required = true)
            @PathVariable String analysisId) {
        try {
            ProjectAnalysis analysis = cacheService.getCachedAnalysis(analysisId);
            if (analysis == null) {
                return ResponseEntity.ok(ThreeDVisualizationResponse.builder()
                        .success(false)
                        .message("分析 ID 不存在")
                        .build());
            }

            return ResponseEntity.ok(ThreeDVisualizationResponse.builder()
                    .success(true)
                    .message("分析 ID 有效")
                    .build());

        } catch (Exception e) {
            log.error("验证分析 ID 失败: {}", analysisId, e);
            return ResponseEntity.internalServerError().body(ThreeDVisualizationResponse.builder()
                    .success(false)
                    .error("验证失败：" + e.getMessage())
                    .build());
        }
    }
}