package com.codesonify.domain.service;

import com.codesonify.domain.entity.ClassMetrics;
import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ProjectAnalysis;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

/**
 * HTML 报告生成器
 *
 * 生成包含代码复杂度分析结果的 HTML 交互式报告
 */
@Slf4j
@Service
public class HtmlReportGenerator {

    /**
     * 生成 HTML 报告
     *
     * @param analysis 项目分析结果
     * @param outputPath 输出文件路径
     * @param midiFileBase64 MIDI 文件 Base64 编码（可选）
     * @throws IOException IO 异常
     */
    public void generateHtmlReport(ProjectAnalysis analysis, String outputPath, String midiFileBase64)
            throws IOException {
        log.info("开始生成 HTML 报告：{}", outputPath);

        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"zh-CN\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>CodeSonify 代码复杂度分析报告 - ").append(escapeHtml(analysis.getProjectName())).append("</title>\n");
        html.append("    <script src=\"https://cdn.jsdelivr.net/npm/chart.js\"></script>\n");
        html.append(getStyles());
        html.append("</head>\n");
        html.append("<body>\n");
        html.append(getHeader(analysis));
        html.append(getOverview(analysis));
        html.append(getComplexityChart(analysis));
        html.append(getClassTable(analysis));
        html.append(getMusicPlayer(midiFileBase64));
        html.append(getFooter());
        html.append("</body>\n");
        html.append("</html>\n");

        // 确保父目录存在
        Path output = Path.of(outputPath);
        if (output.getParent() != null) {
            Files.createDirectories(output.getParent());
        }

        try (FileWriter writer = new FileWriter(outputPath)) {
            writer.write(html.toString());
        }

        log.info("HTML 报告已生成：{}", outputPath);
    }

    /**
     * 生成不带 MIDI 播放器的 HTML 报告
     */
    public void generateHtmlReport(ProjectAnalysis analysis, String outputPath) throws IOException {
        generateHtmlReport(analysis, outputPath, null);
    }

    /**
     * 获取 CSS 样式
     */
    private String getStyles() {
        return """
    <style>
        * { margin: 0; padding: 0; box-sizing: border-box; }
        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
            line-height: 1.6;
            color: #333;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            padding: 20px;
        }
        .container {
            max-width: 1400px;
            margin: 0 auto;
        }
        .card {
            background: white;
            border-radius: 12px;
            box-shadow: 0 10px 40px rgba(0,0,0,0.1);
            padding: 30px;
            margin-bottom: 30px;
        }
        h1 {
            color: white;
            text-align: center;
            font-size: 2.5rem;
            margin-bottom: 30px;
            text-shadow: 2px 2px 4px rgba(0,0,0,0.2);
        }
        h2 {
            color: #4a4a4a;
            border-bottom: 3px solid #667eea;
            padding-bottom: 10px;
            margin-bottom: 20px;
        }
        .stats-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
            gap: 20px;
            margin-bottom: 30px;
        }
        .stat-card {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: white;
            padding: 20px;
            border-radius: 10px;
            text-align: center;
        }
        .stat-value {
            font-size: 2.5rem;
            font-weight: bold;
        }
        .stat-label {
            font-size: 0.9rem;
            opacity: 0.9;
        }
        .chart-container {
            position: relative;
            height: 400px;
            margin: 30px 0;
        }
        table {
            width: 100%;
            border-collapse: collapse;
            margin-top: 20px;
        }
        th, td {
            padding: 12px;
            text-align: left;
            border-bottom: 1px solid #eee;
        }
        th {
            background: #f8f9fa;
            font-weight: 600;
            cursor: pointer;
            user-select: none;
        }
        th:hover {
            background: #e9ecef;
        }
        tr:hover {
            background: #f8f9fa;
        }
        .level-simple { color: #28a745; font-weight: bold; }
        .level-moderate { color: #ffc107; font-weight: bold; }
        .level-complex { color: #fd7e14; font-weight: bold; }
        .level-very-complex { color: #dc3545; font-weight: bold; }
        .music-player {
            background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
            color: white;
            padding: 30px;
            border-radius: 12px;
            text-align: center;
            margin: 30px 0;
        }
        .music-player h3 {
            margin-bottom: 20px;
        }
        .music-player audio {
            width: 100%;
            max-width: 500px;
        }
        footer {
            text-align: center;
            color: white;
            padding: 20px;
            margin-top: 30px;
        }
        .tooltip {
            position: relative;
            cursor: help;
            border-bottom: 1px dotted #667eea;
        }
        @media (max-width: 768px) {
            h1 { font-size: 1.8rem; }
            .stats-grid { grid-template-columns: repeat(2, 1fr); }
        }
    </style>
    """;
    }

    /**
     * 获取页眉
     */
    private String getHeader(ProjectAnalysis analysis) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return """
        <div class="container">
            <h1>🎵 CodeSonify 代码复杂度分析报告</h1>
            <div class="card">
                <h2>项目信息</h2>
                <table>
                    <tr><td width="150"><strong>项目名称</strong></td><td>%s</td></tr>
                    <tr><td><strong>项目路径</strong></td><td>%s</td></tr>
                    <tr><td><strong>分析时间</strong></td><td>%s</td></tr>
                </table>
            </div>
        """.formatted(
                escapeHtml(analysis.getProjectName()),
                escapeHtml(analysis.getProjectPath()),
                timestamp
        );
    }

    /**
     * 获取概览统计
     */
    private String getOverview(ProjectAnalysis analysis) {
        var stats = analysis.getStatistics();
        return """
        <div class="card">
            <h2>📊 项目概览</h2>
            <div class="stats-grid">
                <div class="stat-card">
                    <div class="stat-value">%d</div>
                    <div class="stat-label">总类数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">%d</div>
                    <div class="stat-label">总方法数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">%.1f</div>
                    <div class="stat-label">平均复杂度</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">%d</div>
                    <div class="stat-label">最大复杂度</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">%d</div>
                    <div class="stat-label">总代码行数</div>
                </div>
                <div class="stat-card">
                    <div class="stat-value">%d</div>
                    <div class="stat-label">循环依赖数</div>
                </div>
            </div>
        </div>
        """.formatted(
                stats.getTotalClasses(),
                stats.getTotalMethods(),
                stats.getAverageCyclomaticComplexity(),
                stats.getMaxCyclomaticComplexity(),
                stats.getTotalLinesOfCode(),
                stats.getCycleCount()
        );
    }

    /**
     * 获取复杂度分布图表
     */
    private String getComplexityChart(ProjectAnalysis analysis) {
        var distribution = analysis.getStatistics().getComplexityDistribution();
        return """
        <div class="card">
            <h2>📈 复杂度分布</h2>
            <div class="chart-container">
                <canvas id="complexityChart"></canvas>
            </div>
            <script>
                const ctx = document.getElementById('complexityChart').getContext('2d');
                new Chart(ctx, {
                    type: 'doughnut',
                    data: {
                        labels: ['简单 (1-5)', '中等 (6-10)', '复杂 (11-20)', '非常复杂 (20+)'],
                        datasets: [{
                            data: [%d, %d, %d, %d],
                            backgroundColor: ['#28a745', '#ffc107', '#fd7e14', '#dc3545'],
                            borderWidth: 2
                        }]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        plugins: {
                            legend: {
                                position: 'bottom',
                                labels: {
                                    padding: 20,
                                    font: { size: 14 }
                                }
                            },
                            title: {
                                display: true,
                                text: '方法复杂度等级分布',
                                font: { size: 18 }
                            }
                        }
                    }
                });
            </script>
        </div>
        """.formatted(
                distribution.getSimple(),
                distribution.getModerate(),
                distribution.getComplex(),
                distribution.getVeryComplex()
        );
    }

    /**
     * 获取类列表表格
     */
    private String getClassTable(ProjectAnalysis analysis) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div class=\"card\">\n");
        sb.append("    <h2>📋 类复杂度详情</h2>\n");
        sb.append("    <table id=\"classTable\">\n");
        sb.append("        <thead>\n");
        sb.append("            <tr>\n");
        sb.append("                <th onclick=\"sortTable(0)\">类名</th>\n");
        sb.append("                <th onclick=\"sortTable(1)\">方法数</th>\n");
        sb.append("                <th onclick=\"sortTable(2)\">平均复杂度</th>\n");
        sb.append("                <th onclick=\"sortTable(3)\">最大复杂度</th>\n");
        sb.append("                <th onclick=\"sortTable(4)\">代码行数</th>\n");
        sb.append("                <th onclick=\"sortTable(5)\">耦合度</th>\n");
        sb.append("            </tr>\n");
        sb.append("        </thead>\n");
        sb.append("        <tbody>\n");

        for (ClassMetrics metrics : analysis.getClasses()) {
            String levelClass = getLevelClass(metrics.getMaxComplexity());
            sb.append("            <tr>\n");
            sb.append("                <td><strong>").append(escapeHtml(metrics.getClassName())).append("</strong></td>\n");
            sb.append("                <td>").append(metrics.getTotalMethods()).append("</td>\n");
            sb.append("                <td>").append(String.format("%.1f", metrics.getAverageComplexity())).append("</td>\n");
            sb.append("                <td class=\"").append(levelClass).append("\">").append(metrics.getMaxComplexity()).append("</td>\n");
            sb.append("                <td>").append(metrics.getLinesOfCode()).append("</td>\n");
            sb.append("                <td>").append(metrics.getCouplingBetweenObjects()).append("</td>\n");
            sb.append("            </tr>\n");
        }

        sb.append("        </tbody>\n");
        sb.append("    </table>\n");
        sb.append("    <script>\n");
        sb.append("        function sortTable(column) {\n");
        sb.append("            const table = document.getElementById('classTable');\n");
        sb.append("            const rows = Array.from(table.querySelectorAll('tbody tr'));\n");
        sb.append("            rows.sort((a, b) => {\n");
        sb.append("                const aVal = parseFloat(a.cells[column].textContent) || 0;\n");
        sb.append("                const bVal = parseFloat(b.cells[column].textContent) || 0;\n");
        sb.append("                return bVal - aVal;\n");
        sb.append("            });\n");
        sb.append("            rows.forEach(row => table.querySelector('tbody').appendChild(row));\n");
        sb.append("        }\n");
        sb.append("    </script>\n");
        sb.append("</div>\n");

        return sb.toString();
    }

    /**
     * 获取音乐播放器
     */
    private String getMusicPlayer(String midiBase64) {
        if (midiBase64 == null || midiBase64.isEmpty()) {
            return "";
        }

        return """
        <div class="card music-player">
            <h3>🎹 代码音乐</h3>
            <p style="margin-bottom: 15px;">点击下方播放按钮，聆听代码的复杂度之美</p>
            <audio controls>
                <source src="data:audio/midi;base64,%s" type="audio/midi">
                您的浏览器不支持 MIDI 播放，请尝试下载文件播放。
            </audio>
        </div>
        """.formatted(midiBase64);
    }

    /**
     * 获取页脚
     */
    private String getFooter() {
        return """
        <footer>
            <p>Generated by CodeSonify - 代码复杂度多维度可视化与声音化分析系统</p>
            <p>&copy; 2026 CodeSonify. All rights reserved.</p>
        </footer>
        </div>
        """;
    }

    /**
     * 获取复杂度等级对应的 CSS 类
     */
    private String getLevelClass(int complexity) {
        return switch (ComplexityLevel.fromComplexity(complexity)) {
            case SIMPLE -> "level-simple";
            case MODERATE -> "level-moderate";
            case COMPLEX -> "level-complex";
            case VERY_COMPLEX -> "level-very-complex";
        };
    }

    /**
     * HTML 转义
     */
    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
    }
}
