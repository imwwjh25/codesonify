package com.codesonify.domain.service;

import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Java 文件解析器单元测试
 */
class JavaFileParserTest {

    private JavaFileParser parser;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        parser = new JavaFileParser();
    }

    @Test
    @DisplayName("解析单个 Java 文件")
    void testParseFile() throws Exception {
        File javaFile = createTempJavaFile("""
            package com.test;

            public class HelloWorld {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
            """);

        CompilationUnit cu = parser.parseFile(javaFile);

        assertNotNull(cu, "解析结果不应为空");
        assertTrue(cu.getPackageDeclaration().isPresent(), "应该有包声明");
        assertEquals("com.test", cu.getPackageDeclaration().get().getNameAsString());
        assertFalse(cu.getTypes().isEmpty(), "应该有主类名");
        assertEquals("HelloWorld", cu.getTypes().get(0).getNameAsString());
    }

    @Test
    @DisplayName("解析文件到 SourceFile 实体")
    void testParseFileToSource() throws Exception {
        File javaFile = createTempJavaFile("""
            package com.test;

            public class TestClass {
                private String name;

                public String getName() {
                    return name;
                }
            }
            """);

        var sourceFile = parser.parseFileToSource(javaFile);

        assertNotNull(sourceFile);
        assertEquals("TestClass", sourceFile.getClassName());
        assertEquals("com.test", sourceFile.getPackageName());
        assertNotNull(sourceFile.getCompilationUnit());
    }

    @Test
    @DisplayName("扫描目录中的 Java 文件")
    void testScanJavaFiles() throws Exception {
        // 创建测试目录结构
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);

        createTempJavaFile(srcDir, "Class1", """
            public class Class1 {}
            """);
        createTempJavaFile(srcDir, "Class2", """
            public class Class2 {}
            """);
        createTempJavaFile(srcDir, "Class3", """
            public class Class3 {}
            """);

        List<File> javaFiles = parser.scanJavaFiles(srcDir);

        assertEquals(3, javaFiles.size(), "应该找到 3 个 Java 文件");
    }

    @Test
    @DisplayName("忽略 target 目录中的文件")
    void testIgnoreTargetDirectory() throws Exception {
        Path srcDir = tempDir.resolve("src");
        Path targetDir = tempDir.resolve("target");
        Files.createDirectories(srcDir);
        Files.createDirectories(targetDir);

        createTempJavaFile(srcDir, "Main", "public class Main {}");
        createTempJavaFile(targetDir, "Compiled", "public class Compiled {}");

        List<File> javaFiles = parser.scanJavaFiles(tempDir);

        assertEquals(1, javaFiles.size(), "应该只找到 src 目录中的文件");
        assertEquals("Main.java", javaFiles.get(0).getName());
    }

    @Test
    @DisplayName("解析整个项目")
    void testParseProject() throws Exception {
        Path srcDir = tempDir.resolve("src");
        Files.createDirectories(srcDir);

        createTempJavaFile(srcDir, "ClassA", """
            package com.test;
            public class ClassA {
                public void methodA() {}
            }
            """);
        createTempJavaFile(srcDir, "ClassB", """
            package com.test;
            public class ClassB {
                public void methodB() {}
            }
            """);

        var sourceFiles = parser.parseProject(srcDir);

        assertEquals(2, sourceFiles.size(), "应该解析 2 个文件");
    }

    @Test
    @DisplayName("文件不存在时抛出异常")
    void testFileNotFound() {
        File nonExistentFile = new File("/non/existent/File.java");

        assertThrows(IOException.class, () -> {
            parser.parseFile(nonExistentFile);
        });
    }

    @Test
    @DisplayName("非 Java 文件抛出异常")
    void testNotJavaFile() throws Exception {
        File txtFile = Files.createFile(tempDir.resolve("test.txt")).toFile();

        assertThrows(IOException.class, () -> {
            parser.parseFile(txtFile);
        });
    }

    @Test
    @DisplayName("解析有语法错误的文件时抛出异常")
    void testSyntaxError() throws Exception {
        File javaFile = createTempJavaFile("""
            public class BadSyntax {
                public void brokenMethod( {
                    // 缺少右括号
                }
            }
            """);

        assertThrows(com.github.javaparser.ParseProblemException.class, () -> {
            parser.parseFile(javaFile);
        });
    }

    /**
     * 创建临时 Java 文件
     */
    private File createTempJavaFile(String content) throws IOException {
        return createTempJavaFile(tempDir, "TestClass", content);
    }

    /**
     * 在指定目录创建临时 Java 文件
     */
    private File createTempJavaFile(Path dir, String className, String content) throws IOException {
        Path filePath = dir.resolve(className + ".java");
        Files.writeString(filePath, content);
        return filePath.toFile();
    }
}
