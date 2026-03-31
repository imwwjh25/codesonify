package com.codesonify.domain.entity;

import lombok.Data;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.alg.cycle.CycleDetector;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 依赖图数据结构
 *
 * 使用 JGraphT 构建和分析类之间的依赖关系
 */
@Data
public class DependencyGraph {

    /**
     * 有向图
     */
    private final DefaultDirectedGraph<String, DefaultEdge> graph;

    /**
     * 入度映射（被依赖次数）
     */
    private Map<String, Integer> inDegree;

    /**
     * 出度映射（依赖他人次数）
     */
    private Map<String, Integer> outDegree;

    /**
     * 循环依赖路径
     */
    private List<List<String>> cycles;

    /**
     * 所有节点
     */
    private Set<String> nodes;

    public DependencyGraph() {
        this.graph = new DefaultDirectedGraph<>(DefaultEdge.class);
        this.inDegree = new HashMap<>();
        this.outDegree = new HashMap<>();
        this.cycles = new ArrayList<>();
        this.nodes = new HashSet<>();
    }

    /**
     * 添加节点（类）
     *
     * @param className 类名
     */
    public void addNode(String className) {
        graph.addVertex(className);
        nodes.add(className);
    }

    /**
     * 添加依赖边
     *
     * @param from 依赖源类
     * @param to   被依赖类
     */
    public void addEdge(String from, String to) {
        if (graph.containsVertex(from) && graph.containsVertex(to)) {
            graph.addEdge(from, to);
        }
    }

    /**
     * 计算图的指标
     */
    public void calculateMetrics() {
        // 计算入度和出度
        for (String vertex : graph.vertexSet()) {
            inDegree.put(vertex, graph.inDegreeOf(vertex));
            outDegree.put(vertex, graph.outDegreeOf(vertex));
        }

        // 检测循环依赖
        detectCycles();
    }

    /**
     * 检测循环依赖
     */
    private void detectCycles() {
        CycleDetector<String, DefaultEdge> cycleDetector = new CycleDetector<>(graph);

        if (cycleDetector.detectCycles()) {
            Set<String> cycleVertices = cycleDetector.findCycles();
            // 提取循环路径
            extractCycles(cycleVertices);
        }
    }

    /**
     * 提取循环路径
     *
     * @param cycleVertices 参与循环的节点
     */
    private void extractCycles(Set<String> cycleVertices) {
        // 简化的循环提取，实际实现可能需要更复杂的算法
        for (String vertex : cycleVertices) {
            Set<DefaultEdge> outgoingEdges = graph.outgoingEdgesOf(vertex);
            for (DefaultEdge edge : outgoingEdges) {
                String target = graph.getEdgeTarget(edge);
                if (cycleVertices.contains(target)) {
                    // 检查是否形成环
                    if (hasPath(target, vertex)) {
                        List<String> cycle = findCyclePath(vertex, target);
                        if (cycle != null && !cycle.isEmpty()) {
                            cycles.add(cycle);
                        }
                    }
                }
            }
        }
    }

    /**
     * 检查是否存在从 from 到 to 的路径
     */
    private boolean hasPath(String from, String to) {
        if (from.equals(to)) return true;

        Set<String> visited = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        queue.offer(from);
        visited.add(from);

        while (!queue.isEmpty()) {
            String current = queue.poll();
            Set<DefaultEdge> edges = graph.outgoingEdgesOf(current);

            for (DefaultEdge edge : edges) {
                String target = graph.getEdgeTarget(edge);
                if (target.equals(to)) {
                    return true;
                }
                if (!visited.contains(target)) {
                    visited.add(target);
                    queue.offer(target);
                }
            }
        }

        return false;
    }

    /**
     * 查找循环路径
     */
    private List<String> findCyclePath(String start, String end) {
        // 简化的路径查找
        List<String> path = new ArrayList<>();
        path.add(start);

        Set<DefaultEdge> edges = graph.outgoingEdgesOf(start);
        for (DefaultEdge edge : edges) {
            String target = graph.getEdgeTarget(edge);
            if (target.equals(end)) {
                path.add(target);
                return path;
            }
        }

        return path;
    }

    /**
     * 获取最耦合的类（出度最高）
     *
     * @param limit 返回数量
     * @return 最耦合的类列表
     */
    public List<String> getMostCoupledClasses(int limit) {
        return outDegree.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取被依赖最多的类（入度最高）
     *
     * @param limit 返回数量
     * @return 被依赖最多的类列表
     */
    public List<String> getMostDependedClasses(int limit) {
        return inDegree.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * 获取图的统计信息
     *
     * @return 统计信息
     */
    public GraphStatistics getStatistics() {
        return GraphStatistics.builder()
                .nodeCount(graph.vertexSet().size())
                .edgeCount(graph.edgeSet().size())
                .cycleCount(cycles.size())
                .averageOutDegree(outDegree.values().stream()
                        .mapToDouble(Double::valueOf)
                        .average()
                        .orElse(0.0))
                .averageInDegree(inDegree.values().stream()
                        .mapToDouble(Double::valueOf)
                        .average()
                        .orElse(0.0))
                .build();
    }

    /**
     * 图统计信息
     */
    @Data
    @Builder
    public static class GraphStatistics {
        private int nodeCount;
        private int edgeCount;
        private int cycleCount;
        private double averageOutDegree;
        private double averageInDegree;
    }
}
