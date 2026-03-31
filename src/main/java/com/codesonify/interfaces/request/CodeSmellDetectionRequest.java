package com.codesonify.interfaces.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 代码异味检测请求
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "代码异味检测请求")
public class CodeSmellDetectionRequest {

    @NotBlank(message = "项目路径不能为空")
    @Schema(description = "项目路径", example = "/path/to/project")
    private String projectPath;

    @Schema(description = "项目名称", example = "my-project")
    private String projectName;

    @Schema(description = "是否检测重复代码", example = "true")
    @Builder.Default
    private boolean detectDuplicateCode = true;

    @Schema(description = "长方法最大行数", example = "50")
    private Integer longMethodMaxLines;

    @Schema(description = "长方法最大嵌套深度", example = "4")
    private Integer longMethodMaxNestingDepth;

    @Schema(description = "长方法最大圈复杂度", example = "10")
    private Integer longMethodMaxComplexity;

    @Schema(description = "大类最大行数", example = "300")
    private Integer largeClassMaxLines;

    @Schema(description = "大类最大方法数", example = "15")
    private Integer largeClassMaxMethods;

    @Schema(description = "大类最大字段数", example = "20")
    private Integer largeClassMaxFields;

    @Schema(description = "长参数列表最大参数数", example = "5")
    private Integer longParameterListMaxParams;

    @Schema(description = "高耦合最大 CBO", example = "10")
    private Integer highCouplingMaxCBO;
}