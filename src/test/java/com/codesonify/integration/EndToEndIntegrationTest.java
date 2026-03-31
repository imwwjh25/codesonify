package com.codesonify.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 端到端集成测试
 *
 * 测试完整的代码分析、声音化、报告生成流程
 *
 * 注意：以下测试验证 API 端点的可访问性和基本响应格式
 * 实际的缓存和 Redis 功能需要 Redis 服务器才能正常工作
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("端到端集成测试")
class EndToEndIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private String testAnalysisId;

    @BeforeEach
    void setUp() {
        testAnalysisId = "test-" + System.currentTimeMillis();
    }

    @Test
    @DisplayName("测试完整分析流程")
    void testFullAnalysisFlow() throws Exception {
        // 1. 创建一个测试项目目录
        String testProjectPath = createTestProject();

        // 2. 调用分析 API
        String analyzeResponse = mockMvc.perform(post("/api/analysis/analyze")
                        .param("projectPath", testProjectPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn().getResponse().getContentAsString();

        // 3. 打印响应（实际项目中应该使用 JSON 解析库提取 analysisId）
        System.out.println("分析响应：" + analyzeResponse);

        // 4. 验证响应包含 expected 字段
        mockMvc.perform(post("/api/analysis/analyze")
                        .param("projectPath", testProjectPath)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.projectAnalysis").exists());
    }

    @Test
    @DisplayName("测试分析不存在的项目路径")
    void testAnalyzeNonExistentProject() throws Exception {
        mockMvc.perform(post("/api/analysis/analyze")
                        .param("projectPath", "/non/existent/path")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("测试获取不存在的分析结果")
    void testGetNonExistentAnalysis() throws Exception {
        mockMvc.perform(get("/api/analysis/non-existent-id"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("测试声音化不存在的分析 ID")
    void testSonifyNonExistentAnalysis() throws Exception {
        mockMvc.perform(post("/api/sonification/generate")
                        .param("analysisId", "non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("测试报告生成不存在的分析 ID")
    void testReportNonExistentAnalysis() throws Exception {
        mockMvc.perform(post("/api/report/generate")
                        .param("analysisId", "non-existent-id")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @DisplayName("测试 HTML 报告生成（API 端点验证）")
    void testHtmlReportGeneration() throws Exception {
        // 验证 API 端点返回正确的错误（分析结果不存在）
        mockMvc.perform(get("/api/report/html")
                        .param("analysisId", "non-existent-id")
                        .contentType(MediaType.TEXT_HTML))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试 Draw.io 图表生成（API 端点验证）")
    void testDrawioDiagramGeneration() throws Exception {
        // 验证 API 端点返回正确的错误（分析结果不存在）
        mockMvc.perform(get("/api/report/diagram")
                        .param("analysisId", "non-existent-id")
                        .contentType(MediaType.APPLICATION_XML))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("测试 Swagger API 文档可访问")
    void testSwaggerDocumentation() throws Exception {
        // Swagger 会重定向到 /swagger-ui/index.html
        mockMvc.perform(get("/swagger-ui.html"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @DisplayName("测试静态页面可访问")
    void testStaticPageAccess() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }

    /**
     * 创建测试项目目录
     */
    private String createTestProject() throws Exception {
        java.nio.file.Path tempDir = java.nio.file.Files.createTempDirectory("codesonify-test");

        // 创建一个简单的 Java 文件
        java.nio.file.Path javaFile = tempDir.resolve("TestClass.java");
        java.nio.file.Files.write(javaFile, List.of(
            "package com.test;",
            "public class TestClass {",
            "    public void simpleMethod() {",
            "        System.out.println(\"Hello\");",
            "    }",
            "}"
        ));

        return tempDir.toString();
    }
}
