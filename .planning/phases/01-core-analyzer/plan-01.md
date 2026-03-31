---
plan_id: 1.1
plan_number: 1
wave: 1
description: 实现 Java 文件解析器
status: pending
---

# Plan 1.1: 实现 Java 文件解析器

## 目标

创建一个 Java 文件解析器，能够读取 Java 源代码文件并生成 AST。

## 任务

### Task 1: 添加 JavaParser 依赖

**文件**: `pom.xml`

**工作**: 确认 JavaParser 依赖已添加（已在 pom.xml 中添加）。

### Task 2: 创建 JavaFileParser 类

**文件**: `src/main/java/com/codesonify/domain/service/JavaFileParser.java`

**工作**:
- 创建 `JavaFileParser` 服务类
- 实现 `parseFile(File file)` 方法，返回 `CompilationUnit`
- 实现 `parseProject(Path projectPath)` 方法，扫描所有 `.java` 文件
- 添加异常处理和日志记录

**质量要求**:
- 支持 UTF-8 编码
- 处理语法错误时返回友好的错误信息
- 使用 Spring `@Service` 注解

### Task 3: 创建 SourceFile 实体

**文件**: `src/main/java/com/codesonify/domain/entity/SourceFile.java`

**工作**:
- 创建 `SourceFile` 值对象
- 属性：`filePath`, `packageName`, `className`, `compilationUnit`
- 添加构造函数和 getter 方法

## 验收标准

- [ ] `JavaFileParser` 能够成功解析单个 Java 文件
- [ ] 能够递归扫描目录中的所有 Java 文件
- [ ] 单元测试验证解析结果正确

## 技术笔记

使用 JavaParser 的 `StaticJavaParser.parseFile()` 方法：

```java
CompilationUnit cu = StaticJavaParser.parseFile(
    file, 
    ParserConfiguration.LanguageLevel.JAVA_17
);
```

## Commit 信息

```
feat: 实现 Java 文件解析器

- 添加 JavaFileParser 服务类
- 支持单文件和项目级解析
- 添加 SourceFile 实体
```
