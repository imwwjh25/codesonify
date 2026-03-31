package com.codesonify.domain.entity;

import com.github.javaparser.ast.CompilationUnit;
import lombok.Builder;
import lombok.Data;

/**
 * 源文件实体
 *
 * 表示一个解析后的 Java 源文件
 */
@Data
@Builder
public class SourceFile {

    /**
     * 文件路径
     */
    private String filePath;

    /**
     * 包名
     */
    private String packageName;

    /**
     * 类名
     */
    private String className;

    /**
     * 编译单元（AST 根节点）
     */
    private CompilationUnit compilationUnit;

    /**
     * 获取包路径（将包名转换为路径格式）
     *
     * @return 包路径
     */
    public String getPackagePath() {
        if (packageName == null || packageName.isEmpty()) {
            return "";
        }
        return packageName.replace('.', '/');
    }

    /**
     * 获取完全限定类名
     *
     * @return 包名。类名
     */
    public String getFullyQualifiedName() {
        if (packageName == null || packageName.isEmpty()) {
            return className;
        }
        return packageName + "." + className;
    }
}
