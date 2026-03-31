package com.codesonify.application.service;

import com.codesonify.domain.entity.ProjectAnalysis;
import com.codesonify.domain.service.DependencyGraphExporter;
import com.codesonify.domain.service.HtmlReportGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * 报告生成应用服务
 *
 * 编排领域服务，提供报告生成的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportGenerationService {

    private final HtmlReportGenerator htmlReportGenerator;
    private final DependencyGraphExporter dependencyGraphExporter;

    /**
     * 生成完整的分析报告（HTML + Draw.io）
     *
     * @param analysis 项目分析结果
     * @param outputDir 输出目录
     * @param midiFileBase64 MIDI 文件 Base64 编码（可选）
     * @throws IOException IO 异常
     */
    public void generateFullReport(ProjectAnalysis analysis, String outputDir, String midiFileBase64)
            throws IOException {
        log.info("开始生成完整报告，输出目录：{}", outputDir);

        // 生成 HTML 报告
        String htmlPath = outputDir + "/report.html";
        htmlReportGenerator.generateHtmlReport(analysis, htmlPath, midiFileBase64);

        // 生成 Draw.io 依赖图
        String drawioPath = outputDir + "/dependencies.drawio";
        dependencyGraphExporter.exportToDrawioWithMetrics(
                analysis.getDependencyGraph(),
                analysis.getClasses(),
                drawioPath
        );

        log.info("完整报告已生成：{}", outputDir);
    }

    /**
     * 生成 HTML 报告
     *
     * @param analysis 项目分析结果
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     */
    public void generateHtmlReport(ProjectAnalysis analysis, String outputPath) throws IOException {
        htmlReportGenerator.generateHtmlReport(analysis, outputPath);
    }

    /**
     * 生成 Draw.io 依赖图
     *
     * @param analysis 项目分析结果
     * @param outputPath 输出文件路径
     * @throws IOException IO 异常
     */
    public void generateDrawioDiagram(ProjectAnalysis analysis, String outputPath) throws IOException {
        dependencyGraphExporter.exportToDrawioWithMetrics(
                analysis.getDependencyGraph(),
                analysis.getClasses(),
                outputPath
        );
    }
}
