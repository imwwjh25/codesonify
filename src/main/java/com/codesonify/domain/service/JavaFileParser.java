package com.codesonify.domain.service;

import com.codesonify.domain.entity.SourceFile;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Java 文件解析器
 *
 * 使用 JavaParser 库解析 Java 源代码文件，生成抽象语法树 (AST)
 */
@Slf4j
@Service
public class JavaFileParser {

    /**
     * 解析单个 Java 文件
     *
     * @param file Java 文件
     * @return 编译单元（AST 根节点）
     * @throws IOException 文件读取失败
     * @throws ParseProblemException 语法错误
     */
    public CompilationUnit parseFile(File file) throws IOException {
        log.debug("解析文件：{}", file.getAbsolutePath());

        if (!file.exists()) {
            throw new IOException("文件不存在：" + file.getAbsolutePath());
        }

        if (!file.getName().endsWith(".java")) {
            throw new IOException("不是 Java 文件：" + file.getName());
        }

        try (FileInputStream fis = new FileInputStream(file)) {
            CompilationUnit cu = StaticJavaParser.parse(fis);
            log.debug("文件解析成功：{}", file.getName());
            return cu;
        } catch (ParseProblemException e) {
            log.error("语法解析失败：{} - {}", file.getName(), e.getMessage());
            throw e;
        }
    }

    /**
     * 解析单个 Java 文件并返回 SourceFile 实体
     *
     * @param file Java 文件
     * @return SourceFile 实体
     */
    public SourceFile parseFileToSource(File file) throws IOException {
        CompilationUnit cu = parseFile(file);

        String packageName = cu.getPackageDeclaration()
                .map(pd -> pd.getNameAsString())
                .orElse("");

        String className = cu.getPrimaryTypeName()
                .orElse(file.getName().replace(".java", ""));

        return SourceFile.builder()
                .filePath(file.getAbsolutePath())
                .packageName(packageName)
                .className(className)
                .compilationUnit(cu)
                .build();
    }

    /**
     * 递归扫描目录中的所有 Java 文件
     *
     * @param projectPath 项目根目录
     * @return 所有 Java 文件列表
     * @throws IOException 扫描失败
     */
    public List<File> scanJavaFiles(Path projectPath) throws IOException {
        if (!Files.exists(projectPath)) {
            throw new IOException("目录不存在：" + projectPath);
        }

        log.info("扫描项目目录：{}", projectPath);

        try (Stream<Path> walk = Files.walk(projectPath)) {
            List<File> javaFiles = walk
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> !p.toString().contains("/target/"))
                    .filter(p -> !p.toString().contains("/build/"))
                    .filter(p -> !p.toString().contains("/.git/"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            log.info("找到 {} 个 Java 文件", javaFiles.size());
            return javaFiles;
        }
    }

    /**
     * 解析整个项目的 Java 文件
     *
     * @param projectPath 项目根目录
     * @return 所有 SourceFile 列表
     * @throws IOException 解析失败
     */
    public List<SourceFile> parseProject(Path projectPath) throws IOException {
        List<File> javaFiles = scanJavaFiles(projectPath);
        List<SourceFile> sourceFiles = new ArrayList<>(javaFiles.size());

        int successCount = 0;
        int errorCount = 0;

        for (File file : javaFiles) {
            try {
                SourceFile sourceFile = parseFileToSource(file);
                sourceFiles.add(sourceFile);
                successCount++;
            } catch (ParseProblemException e) {
                log.warn("跳过有语法错误的文件：{} - {}", file.getName(), e.getMessage());
                errorCount++;
            } catch (IOException e) {
                log.error("文件解析失败：{}", file.getAbsolutePath(), e);
                errorCount++;
            }
        }

        log.info("项目解析完成：成功={}, 错误={}", successCount, errorCount);
        return sourceFiles;
    }

    /**
     * 解析多个 Java 文件
     *
     * @param files Java 文件列表
     * @return CompilationUnit 列表
     */
    public List<CompilationUnit> parseFiles(List<File> files) {
        List<CompilationUnit> units = new ArrayList<>();

        for (File file : files) {
            try {
                CompilationUnit cu = parseFile(file);
                units.add(cu);
            } catch (Exception e) {
                log.warn("跳过文件 {}: {}", file.getName(), e.getMessage());
            }
        }

        return units;
    }
}
