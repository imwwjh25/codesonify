package com.codesonify.interfaces.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 3D 可视化响应 DTO
 *
 * 用于返回 3D 场景数据
 */
@Data
@Builder
public class ThreeDVisualizationResponse {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 3D 场景数据
     */
    private SceneData sceneData;

    /**
     * 错误信息
     */
    private String error;

    /**
     * 3D 场景数据
     */
    @Data
    @Builder
    public static class SceneData {
        /**
         * 节点列表（代表类）
         */
        private List<NodeDTO> nodes;

        /**
         * 连线列表（代表依赖关系）
         */
        private List<EdgeDTO> edges;

        /**
         * 场景统计信息
         */
        private Statistics statistics;
    }

    /**
     * 节点 DTO
     */
    @Data
    @Builder
    public static class NodeDTO {
        /**
         * 节点 ID（类名）
         */
        private String id;

        /**
         * 3D 坐标 X
         */
        private double x;

        /**
         * 3D 坐标 Y
         */
        private double y;

        /**
         * 3D 坐标 Z
         */
        private double z;

        /**
         * 高度（基于代码行数）
         */
        private double height;

        /**
         * 颜色（基于复杂度等级）
         */
        private String color;

        /**
         * 大小（基于方法数量）
         */
        private double size;

        /**
         * 节点元数据（类详细信息）
         */
        private NodeMetadata metadata;
    }

    /**
     * 节点元数据
     */
    @Data
    @Builder
    public static class NodeMetadata {
        /**
         * 类名
         */
        private String className;

        /**
         * 包名
         */
        private String packageName;

        /**
         * 文件路径
         */
        private String filePath;

        /**
         * 方法总数
         */
        private int totalMethods;

        /**
         * 平均复杂度
         */
        private double averageComplexity;

        /**
         * 最大复杂度
         */
        private int maxComplexity;

        /**
         * 代码行数
         */
        private int linesOfCode;

        /**
         * 耦合度
         */
        private int coupling;

        /**
         * 字段数量
         */
        private int numberOfFields;

        /**
         * 构造函数数量
         */
        private int numberOfConstructors;

        /**
         * 静态方法数量
         */
        private int numberOfStaticMethods;
    }

    /**
     * 连线 DTO
     */
    @Data
    @Builder
    public static class EdgeDTO {
        /**
         * 起始节点 ID
         */
        private String from;

        /**
         * 目标节点 ID
         */
        private String to;

        /**
         * 连线类型
         */
        private EdgeType type;
    }

    /**
     * 连线类型
     */
    public enum EdgeType {
        /**
         * 依赖关系
         */
        DEPENDENCY,
        /**
         * 继承关系
         */
        INHERITANCE,
        /**
         * 实现关系
         */
        IMPLEMENTATION
    }

    /**
     * 场景统计信息
     */
    @Data
    @Builder
    public static class Statistics {
        /**
         * 总节点数
         */
        private int totalNodes;

        /**
         * 总连线数
         */
        private int totalEdges;

        /**
         * 最大高度
         */
        private double maxHeight;

        /**
         * 最小高度
         */
        private double minHeight;
    }
}