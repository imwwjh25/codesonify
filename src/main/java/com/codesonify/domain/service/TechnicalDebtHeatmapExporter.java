package com.codesonify.domain.service;

import com.codesonify.domain.entity.TechnicalDebtScore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 技术债务热力图导出器
 *
 * 生成技术债务热力图，支持 Draw.io 格式
 */
@Slf4j
@Service
public class TechnicalDebtHeatmapExporter {

    /**
     * 导出 Draw.io 格式热力图
     */
    public String exportToDrawio(List<TechnicalDebtScore> scores) {
        log.info("开始生成技术债务热力图，共 {} 个类", scores.size());

        StringBuilder xml = new StringBuilder();

        xml.append("<mxfile host=\"app.diagrams.net\">\n");
        xml.append("  <diagram name=\"Technical Debt Heatmap\" id=\"technical-debt-heatmap\">\n");
        xml.append("    <mxGraphModel dx=\"1422\" dy=\"794\" grid=\"1\" gridSize=\"10\" guides=\"1\" tooltips=\"1\" connect=\"1\" arrows=\"1\" fold=\"1\" page=\"1\" pageScale=\"1\" pageWidth=\"1600\" pageHeight=\"1200\" math=\"0\" shadow=\"0\">\n");
        xml.append("      <root>\n");
        xml.append("        <mxCell id=\"0\" />\n");
        xml.append("        <mxCell id=\"1\" parent=\"0\" />\n");

        // 标题
        xml.append("        <mxCell id=\"title\" value=\"技术债务热力图\" style=\"text;html=1;strokeColor=none;fillColor=none;align=center;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=20;fontStyle=1\" vertex=\"1\" parent=\"1\">\n");
        xml.append("          <mxGeometry x=\"500\" y=\"20\" width=\"600\" height=\"40\" as=\"geometry\" />\n");
        xml.append("        </mxCell>\n");

        // 图例
        appendLegend(xml);

        // 热力图网格
        int colSize = 100;
        int rowSize = 60;
        int startX = 50;
        int startY = 100;
        int maxCols = 15;

        for (int i = 0; i < scores.size(); i++) {
            TechnicalDebtScore score = scores.get(i);
            int row = i / maxCols;
            int col = i % maxCols;
            int x = startX + col * colSize;
            int y = startY + row * rowSize;

            String fillColor = score.getLevelColor();
            String className = score.getClassName();
            if (className.length() > 12) {
                className = className.substring(0, 10) + "...";
            }

            xml.append("        <mxCell id=\"cell-").append(i).append("\" value=\"").append(className)
                    .append("\\n").append(String.format("%.1f", score.getTotalScore()))
                    .append("\" style=\"rounded=1;whiteSpace=wrap;html=1;fillColor=").append(fillColor)
                    .append(";strokeColor=#000000;fontColor=#FFFFFF;fontSize=10;\" vertex=\"1\" parent=\"1\">\n");
            xml.append("          <mxGeometry x=\"").append(x).append("\" y=\"").append(y)
                    .append("\" width=\"").append(colSize - 5).append("\" height=\"").append(rowSize - 5).append("\" as=\"geometry\" />\n");
            xml.append("        </mxCell>\n");

            // 工具提示
            xml.append("        <mxCell id=\"tooltip-").append(i).append("\" value=\"")
                    .append(escapeXml(getTooltipText(score)))
                    .append("\" style=\"text;html=1;strokeColor=none;fillColor=none;align=left;verticalAlign=middle;whiteSpace=wrap;rounded=0;fontSize=9;\" vertex=\"1\" parent=\"1\">\n");
            xml.append("          <mxGeometry x=\"").append(x).append("\" y=\"").append(y + rowSize)
                    .append("\" width=\"300\" height=\"100\" as=\"geometry\" />\n");
            xml.append("        </mxCell>\n");
        }

        // 统计信息
        appendStatistics(xml, scores, startY + ((scores.size() / maxCols) + 1) * rowSize + 50);

        xml.append("      </root>\n");
        xml.append("    </mxGraphModel>\n");
        xml.append("  </diagram>\n");
        xml.append("</mxfile>");

        log.info("技术债务热力图生成完成");
        return xml.toString();
    }

    /**
     * 添加图例
     */
    private void appendLegend(StringBuilder xml) {
        int startX = 50;
        int startY = 65;
        int legendWidth = 80;
        int legendHeight = 20;

        String[] levels = {"无债务 (0-20)", "低债务 (20-40)", "中等债务 (40-60)", "高债务 (60-80)", "严重债务 (80-100)"};
        String[] colors = {"#00C853", "#64DD17", "#FFD600", "#FF6D00", "#D50000"};

        for (int i = 0; i < levels.length; i++) {
            int x = startX + i * 200;
            xml.append("        <mxCell id=\"legend-").append(i).append("\" value=\"").append(levels[i])
                    .append("\" style=\"rounded=1;whiteSpace=wrap;html=1;fillColor=").append(colors[i])
                    .append(";strokeColor=#000000;fontColor=#FFFFFF;fontSize=11;\" vertex=\"1\" parent=\"1\">\n");
            xml.append("          <mxGeometry x=\"").append(x).append("\" y=\"").append(startY)
                    .append("\" width=\"").append(legendWidth).append("\" height=\"").append(legendHeight).append("\" as=\"geometry\" />\n");
            xml.append("        </mxCell>\n");
        }
    }

    /**
     * 添加统计信息
     */
    private void appendStatistics(StringBuilder xml, List<TechnicalDebtScore> scores, int startY) {
        int total = scores.size();
        long criticalCount = scores.stream().filter(s -> s.getDebtLevel() == TechnicalDebtScore.TechnicalDebtLevel.CRITICAL).count();
        long highCount = scores.stream().filter(s -> s.getDebtLevel() == TechnicalDebtScore.TechnicalDebtLevel.HIGH).count();
        long mediumCount = scores.stream().filter(s -> s.getDebtLevel() == TechnicalDebtScore.TechnicalDebtLevel.MEDIUM).count();
        long lowCount = scores.stream().filter(s -> s.getDebtLevel() == TechnicalDebtScore.TechnicalDebtLevel.LOW).count();
        long noneCount = scores.stream().filter(s -> s.getDebtLevel() == TechnicalDebtScore.TechnicalDebtLevel.NONE).count();
        double avgScore = scores.stream().mapToDouble(TechnicalDebtScore::getTotalScore).average().orElse(0);
        int totalEstimatedHours = scores.stream().mapToInt(TechnicalDebtScore::getEstimatedFixHours).sum();

        String statsText = String.format(
                "统计信息: 总类数 %d | 严重 %d | 高 %d | 中 %d | 低 %d | 无 %d | 平均分 %.1f | 预估修复工时 %d 小时",
                total, criticalCount, highCount, mediumCount, lowCount, noneCount, avgScore, totalEstimatedHours
        );

        xml.append("        <mxCell id=\"statistics\" value=\"").append(escapeXml(statsText))
                .append("\" style=\"rounded=1;whiteSpace=wrap;html=1;fillColor=#E3F2FD;strokeColor=#1976D2;fontColor=#0D47A1;fontSize=12;fontStyle=1;\" vertex=\"1\" parent=\"1\">\n");
        xml.append("          <mxGeometry x=\"50\" y=\"").append(startY)
                .append("\" width=\"1500\" height=\"40\" as=\"geometry\" />\n");
        xml.append("        </mxCell>\n");
    }

    /**
     * 获取工具提示文本
     */
    private String getTooltipText(TechnicalDebtScore score) {
        return String.format(
                "类: %s\n包: %s\n总分: %.1f\n债务等级: %s\n复杂度分: %.1f\n耦合度分: %.1f\n代码行数分: %.1f\n嵌套深度分: %.1f\n方法数量分: %.1f\n预估修复工时: %d 小时",
                score.getClassName(),
                score.getPackageName(),
                score.getTotalScore(),
                score.getLevelName(),
                score.getComplexityScore(),
                score.getCouplingScore(),
                score.getLocScore(),
                score.getNestingScore(),
                score.getMethodCountScore(),
                score.getEstimatedFixHours()
        );
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
}