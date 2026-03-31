package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.DependencyGraph;
import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.interfaces.dto.ThreeDVisualizationResponse.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 3D 可视化服务
 *
 * 将代码分析结果转换为 3D 场景数据
 */
@Slf4j
@Service
public class ThreeDVisualizationService {

    /**
     * 3D 点坐标
     */
    @Data
    public static class Point3D {
        private final double x;
        private final double y;
        private final double z;

        public Point3D(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }

    /**
     * 生成 3D 场景数据
     *
     * @param analysis 项目分析结果
     * @return 3D 场景数据
     */
    public SceneData generateSceneData(ProjectAnalysis analysis) {
        log.info("生成 3D 场景数据，项目：{}", analysis.getProjectName());

        List<ClassMetrics> classes = analysis.getClasses();
        DependencyGraph dependencyGraph = analysis.getDependencyGraph();

        // 计算节点位置
        Map<String, Point3D> nodePositions = calculateNodePositions(classes, dependencyGraph);

        // 创建节点列表
        List<NodeDTO> nodes = createNodes(classes, nodePositions);

        // 创建连线列表
        List<EdgeDTO> edges = createEdges(dependencyGraph);

        // 计算统计信息
        Statistics statistics = calculateStatistics(nodes);

        return SceneData.builder()
                .nodes(nodes)
                .edges(edges)
                .statistics(statistics)
                .build();
    }

    /**
     * 计算节点位置（使用圆形布局）
     *
     * @param classes 类列表
     * @param dependencyGraph 依赖图
     * @return 节点位置映射
     */
    private Map<String, Point3D> calculateNodePositions(List<ClassMetrics> classes, DependencyGraph dependencyGraph) {
        Map<String, Point3D> positions = new HashMap<>();
        int totalNodes = classes.size();

        if (totalNodes == 0) {
            return positions;
        }

        // 计算圆半径
        double radius = calculateRadius(totalNodes);

        // 将节点均匀分布在圆周上
        double angleStep = 2 * Math.PI / totalNodes;

        for (int i = 0; i < totalNodes; i++) {
            ClassMetrics classMetrics = classes.get(i);
            double angle = i * angleStep;

            // 计算圆形位置（XZ 平面）
            double x = radius * Math.cos(angle);
            double z = radius * Math.sin(angle);

            // 高度基于代码行数
            double y = calculateHeight(classMetrics.getLinesOfCode());

            positions.put(classMetrics.getClassName(), new Point3D(x, y, z));
        }

        return positions;
    }

    /**
     * 计算圆半径
     *
     * @param nodeCount 节点数量
     * @return 半径
     */
    private double calculateRadius(int nodeCount) {
        // 基础半径 + 每个节点增加的间距
        return 100 + nodeCount * 10;
    }

    /**
     * 计算节点高度（基于代码行数）
     *
     * @param linesOfCode 代码行数
     * @return 高度
     */
    private double calculateHeight(int linesOfCode) {
        // 基础高度 + 代码行数的影响
        return 10 + Math.min(linesOfCode * 0.1, 200);
    }

    /**
     * 创建节点列表
     *
     * @param classes 类列表
     * @param nodePositions 节点位置映射
     * @return 节点列表
     */
    private List<NodeDTO> createNodes(List<ClassMetrics> classes, Map<String, Point3D> nodePositions) {
        List<NodeDTO> nodes = new ArrayList<>();

        for (ClassMetrics classMetrics : classes) {
            Point3D position = nodePositions.get(classMetrics.getClassName());
            if (position == null) {
                continue;
            }

            NodeDTO node = NodeDTO.builder()
                    .id(classMetrics.getClassName())
                    .x(position.getX())
                    .y(position.getY())
                    .z(position.getZ())
                    .height(calculateHeight(classMetrics.getLinesOfCode()))
                    .color(mapComplexityToColor(classMetrics.getMaxComplexity()))
                    .size(calculateSize(classMetrics.getTotalMethods()))
                    .metadata(NodeMetadata.builder()
                            .className(classMetrics.getClassName())
                            .packageName(classMetrics.getPackageName())
                            .filePath(classMetrics.getFilePath())
                            .totalMethods(classMetrics.getTotalMethods())
                            .averageComplexity(classMetrics.getAverageComplexity())
                            .maxComplexity(classMetrics.getMaxComplexity())
                            .linesOfCode(classMetrics.getLinesOfCode())
                            .coupling(classMetrics.getCouplingBetweenObjects())
                            .numberOfFields(classMetrics.getNumberOfFields())
                            .numberOfConstructors(classMetrics.getNumberOfConstructors())
                            .numberOfStaticMethods(classMetrics.getNumberOfStaticMethods())
                            .build())
                    .build();

            nodes.add(node);
        }

        return nodes;
    }

    /**
     * 创建连线列表
     *
     * @param dependencyGraph 依赖图
     * @return 连线列表
     */
    private List<EdgeDTO> createEdges(DependencyGraph dependencyGraph) {
        List<EdgeDTO> edges = new ArrayList<>();

        if (dependencyGraph == null || dependencyGraph.getGraph() == null) {
            return edges;
        }

        // 遍历图的所有边
        dependencyGraph.getGraph().edgeSet().forEach(edge -> {
            String from = dependencyGraph.getGraph().getEdgeSource(edge);
            String to = dependencyGraph.getGraph().getEdgeTarget(edge);

            EdgeDTO edgeDTO = EdgeDTO.builder()
                    .from(from)
                    .to(to)
                    .type(EdgeType.DEPENDENCY)
                    .build();

            edges.add(edgeDTO);
        });

        log.info("创建了 {} 条连线", edges.size());
        return edges;
    }

    /**
     * 计算节点大小（基于方法数量）
     *
     * @param methodCount 方法数量
     * @return 大小
     */
    private double calculateSize(int methodCount) {
        // 基础大小 + 方法数量的影响
        return 1.0 + Math.min(methodCount * 0.1, 5.0);
    }

    /**
     * 映射复杂度到颜色
     *
     * @param complexity 复杂度
     * @return 颜色（十六进制）
     */
    private String mapComplexityToColor(int complexity) {
        ComplexityLevel level = ComplexityLevel.fromComplexity(complexity);

        return switch (level) {
            case SIMPLE -> "#28a745";      // 绿色 - 简单
            case MODERATE -> "#ffc107";    // 黄色 - 中等
            case COMPLEX -> "#fd7e14";     // 橙色 - 复杂
            case VERY_COMPLEX -> "#dc3545"; // 红色 - 非常复杂
        };
    }

    /**
     * 计算场景统计信息
     *
     * @param nodes 节点列表
     * @return 统计信息
     */
    private Statistics calculateStatistics(List<NodeDTO> nodes) {
        if (nodes.isEmpty()) {
            return Statistics.builder()
                    .totalNodes(0)
                    .totalEdges(0)
                    .maxHeight(0)
                    .minHeight(0)
                    .build();
        }

        double maxHeight = nodes.stream()
                .mapToDouble(NodeDTO::getHeight)
                .max()
                .orElse(0);

        double minHeight = nodes.stream()
                .mapToDouble(NodeDTO::getHeight)
                .min()
                .orElse(0);

        return Statistics.builder()
                .totalNodes(nodes.size())
                .totalEdges(0) // 在 createEdges 后设置
                .maxHeight(maxHeight)
                .minHeight(minHeight)
                .build();
    }
}