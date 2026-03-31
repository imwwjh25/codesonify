package com.codesonify.interfaces.controller;

import com.codesonify.application.service.CodeSmellAnalysisService;
import com.codesonify.domain.valueobject.Severity;
import com.codesonify.domain.valueobject.SmellType;
import com.codesonify.interfaces.dto.CodeSmellDTO;
import com.codesonify.interfaces.request.CodeSmellDetectionRequest;
import com.codesonify.interfaces.response.CodeSmellAnalysisResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * CodeSmellController 单元测试
 */
class CodeSmellControllerTest {

    @Mock
    private CodeSmellAnalysisService codeSmellAnalysisService;

    @InjectMocks
    private CodeSmellController controller;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAnalyzeCodeSmells_Success() {
        // 准备测试数据
        CodeSmellDetectionRequest request = CodeSmellDetectionRequest.builder()
                .projectPath("/test/path")
                .projectName("test-project")
                .build();

        CodeSmellAnalysisResponse expectedResponse = CodeSmellAnalysisResponse.builder()
                .analysisId("test-id")
                .projectName("test-project")
                .projectPath("/test/path")
                .totalCount(5)
                .smellScore(50.0)
                .qualityScore(50.0)
                .qualityLevel("中等")
                .build();

        // Mock 行为
        try {
            when(codeSmellAnalysisService.analyzeProject(any())).thenReturn(expectedResponse);
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        // 执行测试
        ResponseEntity<CodeSmellAnalysisResponse> response = controller.analyzeCodeSmells(request);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("test-id", response.getBody().getAnalysisId());
        assertEquals("test-project", response.getBody().getProjectName());
        assertEquals(5, response.getBody().getTotalCount());

        // 验证方法调用
        try {
            verify(codeSmellAnalysisService, times(1)).analyzeProject(any());
        } catch (Exception e) {
            fail("Unexpected exception");
        }
    }

    @Test
    void testAnalyzeCodeSmells_ServiceThrowsException() {
        // 准备测试数据
        CodeSmellDetectionRequest request = CodeSmellDetectionRequest.builder()
                .projectPath("/test/path")
                .build();

        // Mock 行为 - 抛出异常
        try {
            when(codeSmellAnalysisService.analyzeProject(any()))
                    .thenThrow(new RuntimeException("分析失败"));
        } catch (Exception e) {
            fail("Unexpected exception");
        }

        // 执行测试
        ResponseEntity<CodeSmellAnalysisResponse> response = controller.analyzeCodeSmells(request);

        // 验证结果
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetAnalysisResult_Success() {
        // 准备测试数据
        String analysisId = "test-analysis-id";
        CodeSmellAnalysisResponse expectedResponse = CodeSmellAnalysisResponse.builder()
                .analysisId(analysisId)
                .projectName("test-project")
                .totalCount(3)
                .build();

        // Mock 行为
        when(codeSmellAnalysisService.getAnalysisResponse(analysisId))
                .thenReturn(expectedResponse);

        // 执行测试
        ResponseEntity<CodeSmellAnalysisResponse> response = controller.getAnalysisResult(analysisId);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(analysisId, response.getBody().getAnalysisId());

        // 验证方法调用
        verify(codeSmellAnalysisService, times(1)).getAnalysisResponse(analysisId);
    }

    @Test
    void testGetAnalysisResult_NotFound() {
        // 准备测试数据
        String analysisId = "non-existent-id";

        // Mock 行为 - 返回 null
        when(codeSmellAnalysisService.getAnalysisResponse(analysisId))
                .thenReturn(null);

        // 执行测试
        ResponseEntity<CodeSmellAnalysisResponse> response = controller.getAnalysisResult(analysisId);

        // 验证结果
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }

    @Test
    void testGetCodeSmells_Success() {
        // 准备测试数据
        String analysisId = "test-analysis-id";
        List<CodeSmellDTO> expectedSmells = createMockCodeSmellDTOs();

        // Mock 行为
        when(codeSmellAnalysisService.getCodeSmells(analysisId)).thenReturn(expectedSmells);

        // 执行测试
        ResponseEntity<List<CodeSmellDTO>> response = controller.getCodeSmells(analysisId);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // 验证方法调用
        verify(codeSmellAnalysisService, times(1)).getCodeSmells(analysisId);
    }

    @Test
    void testGetCodeSmells_EmptyList() {
        // 准备测试数据
        String analysisId = "test-analysis-id";

        // Mock 行为 - 返回空列表
        when(codeSmellAnalysisService.getCodeSmells(analysisId)).thenReturn(List.of());

        // 执行测试
        ResponseEntity<List<CodeSmellDTO>> response = controller.getCodeSmells(analysisId);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().isEmpty());
    }

    @Test
    void testGetCodeSmellsByType_Success() {
        // 准备测试数据
        String analysisId = "test-analysis-id";
        List<CodeSmellDTO> expectedSmells = createMockCodeSmellDTOs();

        // Mock 行为
        when(codeSmellAnalysisService.getCodeSmellsByType(analysisId, SmellType.LONG_METHOD))
                .thenReturn(expectedSmells);

        // 执行测试
        ResponseEntity<List<CodeSmellDTO>> response =
                controller.getCodeSmellsByType(analysisId, SmellType.LONG_METHOD);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // 验证方法调用
        verify(codeSmellAnalysisService, times(1)).getCodeSmellsByType(analysisId, SmellType.LONG_METHOD);
    }

    @Test
    void testGetCodeSmellsBySeverity_Success() {
        // 准备测试数据
        String analysisId = "test-analysis-id";
        List<CodeSmellDTO> expectedSmells = createMockCodeSmellDTOs();

        // Mock 行为
        when(codeSmellAnalysisService.getCodeSmellsBySeverity(analysisId, Severity.HIGH))
                .thenReturn(expectedSmells);

        // 执行测试
        ResponseEntity<List<CodeSmellDTO>> response =
                controller.getCodeSmellsBySeverity(analysisId, Severity.HIGH);

        // 验证结果
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(2, response.getBody().size());

        // 验证方法调用
        verify(codeSmellAnalysisService, times(1)).getCodeSmellsBySeverity(analysisId, Severity.HIGH);
    }

    /**
     * 创建模拟的代码异味 DTO 列表
     */
    private List<CodeSmellDTO> createMockCodeSmellDTOs() {
        return List.of(
                CodeSmellDTO.builder()
                        .id("1")
                        .type(SmellType.LONG_METHOD)
                        .severity(Severity.HIGH)
                        .className("ClassA")
                        .methodName("method1")
                        .message("长方法")
                        .suggestion("拆分")
                        .build(),
                CodeSmellDTO.builder()
                        .id("2")
                        .type(SmellType.LARGE_CLASS)
                        .severity(Severity.MEDIUM)
                        .className("ClassB")
                        .message("大类")
                        .suggestion("拆分")
                        .build()
        );
    }
}