package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.DependencyGraph;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * 依赖图导出器
 *
 * 将依赖关系图导出为 Draw.io (diagram.io) 格式
 */
@Slf4j
@Service
public class DependencyGraphExporter {

    /**
     * 导出为 Draw.io 格式
     *
     * @param graph 依赖图
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     */
    public void exportToDrawio(DependencyGraph graph, String outputPath) throws IOException {
        log.info("开始导出依赖图到 Draw.io 格式：{}", outputPath);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<mxfile host=\"app.diagrams.net\" modified=\"").append(System.currentTimeMillis())
           .append("\" agent=\"CodeSonify\" etag=\"codesonify\" version=\"21.0.0\" type=\"device\">\n");
        xml.append("  <diagram id=\"dependency-graph\" name=\"依赖关系图\">\n");
        xml.append("    <mxGraphModel dx=\"1200\" dy=\"800\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"1600\" pageHeight=\"900\" math=\"0\" shadow=\"0\">\n");
        xml.append("      <root>\n");

        // 添加根节点
        xml.append("        <mxCell id=\"0\"/>\n");
        xml.append("        <mxCell id=\"1\" parent=\"0\"/>\n");

        // 计算节点位置
        List<String> nodes = new ArrayList<>(graph.getNodes());
        Map<String, NodePosition> positions = calculatePositions(nodes);

        // 添加类节点
        for (String className : nodes) {
            NodePosition pos = positions.get(className);
            String style = getNodeStyle(className, graph);
            xml.append("        <mxCell id=\"").append(className).append("\" value=\"")
               .append(escapeXml(className)).append("\" style=\"").append(style)
               .append("\" vertex=\"1\" parent=\"1\">\n");
            xml.append("          <mxGeometry x=\"").append(pos.x)
               .append("\" y=\"").append(pos.y)
               .append("\" width=\"120\" height=\"60\" as=\"geometry\"/>\n");
            xml.append("        </mxCell>\n");
        }

        // 添加依赖边
        for (String source : nodes) {
            Set<String> targets = getTargets(graph, source);
            for (String target : targets) {
                if (graph.getNodes().contains(target)) {
                    String edgeId = "edge-" + source + "-" + target;
                    xml.append("        <mxCell id=\"").append(edgeId)
                       .append("\" style=\"edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;entryX=0;entryY=0.5;\" edge=\"1\" parent=\"1\" source=\"")
                       .append(source).append("\" target=\"").append(target).append("\">\n");
                    xml.append("          <mxGeometry relative=\"1\" as=\"geometry\"/>\n");
                    xml.append("        </mxCell>\n");
                }
            }
        }

        xml.append("      </root>\n");
        xml.append("    </mxGraphModel>\n");
        xml.append("  </diagram>\n");
        xml.append("</mxfile>\n");

        // 写入文件
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(xml.toString());
        }

        log.info("依赖图已导出：{}", outputPath);
    }

    /**
     * 获取节点的目标依赖
     */
    private Set<String> getTargets(DependencyGraph graph, String source) {
        // 使用反射或其他方式获取依赖图的边信息
        // 这里简化处理，实际需要访问 DependencyGraph 的内部结构
        Set<String> targets = new HashSet<>();

        // 尝试通过 graph 的公共方法获取
        // 由于 DependencyGraph 没有公开获取边的方法，这里返回空集
        // 在实际使用中，需要修改 DependencyGraph 添加获取边的方法

        return targets;
    }

    /**
     * 计算节点位置（圆形布局）
     */
    private Map<String, NodePosition> calculatePositions(List<String> nodes) {
        Map<String, NodePosition> positions = new HashMap<>();

        int centerX = 600;
        int centerY = 400;
        int radius = 300;

        double angleStep = 2 * Math.PI / nodes.size();

        for (int i = 0; i < nodes.size(); i++) {
            String node = nodes.get(i);
            double angle = i * angleStep;

            int x = (int) (centerX + radius * Math.cos(angle) - 60);
            int y = (int) (centerY + radius * Math.sin(angle) - 30);

            positions.put(node, new NodePosition(x, y));
        }

        return positions;
    }

    /**
     * 获取节点样式
     */
    private String getNodeStyle(String className, DependencyGraph graph) {
        // 根据复杂度获取颜色
        // 简化处理：默认样式
        return "rounded=0;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;";
    }

    /**
     * XML 转义
     */
    private String escapeXml(String text) {
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&apos;");
    }

    /**
     * 节点位置
     */
    private static class NodePosition {
        final int x;
        final int y;

        NodePosition(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    /**
     * 导出为带复杂度信息的 Draw.io 格式
     *
     * @param graph 依赖图
     * @param classMetricsList 类指标列表
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     */
    public void exportToDrawioWithMetrics(DependencyGraph graph,
                                         List<ClassMetrics> classMetricsList,
                                         String outputPath) throws IOException {
        log.info("开始导出带复杂度信息的依赖图到 Draw.io 格式：{}", outputPath);

        StringBuilder xml = new StringBuilder();
        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<mxfile host=\"app.diagrams.net\" modified=\"").append(System.currentTimeMillis())
           .append("\" agent=\"CodeSonify\" etag=\"codesonify\" version=\"21.0.0\" type=\"device\">\n");
        xml.append("  <diagram id=\"dependency-graph\" name=\"依赖关系图\">\n");
        xml.append("    <mxGraphModel dx=\"1200\" dy=\"800\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"1600\" pageHeight=\"900\" math=\"0\" shadow=\"0\">\n");
        xml.append("      <root>\n");

        // 添加根节点
        xml.append("        <mxCell id=\"0\"/>\n");
        xml.append("        <mxCell id=\"1\" parent=\"0\"/>\n");

        // 构建类名到指标的映射
        Map<String, ClassMetrics> metricsMap = new HashMap<>();
        for (ClassMetrics metrics : classMetricsList) {
            metricsMap.put(metrics.getClassName(), metrics);
        }

        // 计算节点位置
        List<String> nodes = new ArrayList<>(graph.getNodes());
        Map<String, NodePosition> positions = calculatePositions(nodes);

        // 添加类节点（带复杂度信息）
        for (String className : nodes) {
            NodePosition pos = positions.get(className);
            ClassMetrics metrics = metricsMap.get(className);
            String label = buildLabel(className, metrics);
            String style = getNodeStyleWithMetrics(metrics);

            xml.append("        <mxCell id=\"").append(className).append("\" value=\"")
               .append(escapeXml(label)).append("\" style=\"").append(style)
               .append("\" vertex=\"1\" parent=\"1\">\n");
            xml.append("          <mxGeometry x=\"").append(pos.x)
               .append("\" y=\"").append(pos.y)
               .append("\" width=\"140\" height=\"80\" as=\"geometry\"/>\n");
            xml.append("        </mxCell>\n");
        }

        // 添加依赖边
        int edgeCount = 0;
        for (String source : nodes) {
            ClassMetrics sourceMetrics = metricsMap.get(source);
            if (sourceMetrics != null) {
                for (String target : sourceMetrics.getDependencies()) {
                    if (graph.getNodes().contains(target)) {
                        String edgeId = "edge-" + (edgeCount++);
                        xml.append("        <mxCell id=\"").append(edgeId)
                           .append("\" style=\"edgeStyle=orthogonalEdgeStyle;rounded=0;orthogonalLoop=1;jettySize=auto;html=1;exitX=1;exitY=0.5;entryX=0;entryY=0.5;\" edge=\"1\" parent=\"1\" source=\"")
                           .append(source).append("\" target=\"").append(target).append("\">\n");
                        xml.append("          <mxGeometry relative=\"1\" as=\"geometry\"/>\n");
                        xml.append("        </mxCell>\n");
                    }
                }
            }
        }

        xml.append("      </root>\n");
        xml.append("    </mxGraphModel>\n");
        xml.append("  </diagram>\n");
        xml.append("</mxfile>\n");

        // 写入文件
        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(xml.toString());
        }

        log.info("依赖图已导出：{}", outputPath);
    }

    /**
     * 构建节点标签
     */
    private String buildLabel(String className, ClassMetrics metrics) {
        if (metrics == null) {
            return className;
        }
        return String.format("<b>%s</b><br/>CC: %d<br/>LOC: %d<br/>CBO: %d",
                className,
                (int) metrics.getMaxComplexity(),
                metrics.getLinesOfCode(),
                metrics.getCouplingBetweenObjects());
    }

    /**
     * 根据复杂度获取节点样式
     */
    private String getNodeStyleWithMetrics(ClassMetrics metrics) {
        if (metrics == null) {
            return "rounded=0;whiteSpace=wrap;html=1;fillColor=#dae8fc;strokeColor=#6c8ebf;";
        }

        int maxComplexity = metrics.getMaxComplexity();
        ComplexityLevel level = ComplexityLevel.fromComplexity(maxComplexity);

        return switch (level) {
            case SIMPLE ->
                "rounded=0;whiteSpace=wrap;html=1;fillColor=#d5e8d4;strokeColor=#82b366;fontStyle=0";
            case MODERATE ->
                "rounded=0;whiteSpace=wrap;html=1;fillColor=#fff2cc;strokeColor=#d6b656;fontStyle=0";
            case COMPLEX ->
                "rounded=0;whiteSpace=wrap;html=1;fillColor=#f8cecc;strokeColor=#b85450;fontStyle=0";
            case VERY_COMPLEX ->
                "rounded=0;whiteSpace=wrap;html=1;fillColor=#ea6b66;strokeColor=#36393d;fontColor=#ffffff;fontStyle=1";
        };
    }
}
