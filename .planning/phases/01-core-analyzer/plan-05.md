---
plan_id: 1.5
plan_number: 5
wave: 3
description: 编写单元测试
status: pending
---

# Plan 1.5: 编写单元测试

## 目标

为核心分析引擎编写完整的单元测试，确保代码质量。

## 任务

### Task 1: CyclomaticComplexityCalculatorTest

**文件**: `src/test/java/com/codesonify/domain/service/CyclomaticComplexityCalculatorTest.java`

**工作**:
- 测试简单方法 (CC=1)
- 测试包含 if/else 的方法
- 测试包含循环的方法
- 测试包含逻辑运算符的方法

### Task 2: JavaFileParserTest

**文件**: `src/test/java/com/codesonify/domain/service/JavaFileParserTest.java`

**工作**:
- 测试单文件解析
- 测试项目目录扫描
- 测试语法错误处理

### Task 3: DependencyAnalyzerTest

**文件**: `src/test/java/com/codesonify/domain/service/DependencyAnalyzerTest.java`

**工作**:
- 测试依赖提取
- 测试循环依赖检测
- 测试 CBO 计算

### Task 4: CodeAnalysisServiceTest

**文件**: `src/test/java/com/codesonify/application/service/CodeAnalysisServiceTest.java`

**工作**:
- 测试完整的分析流程
- 集成测试

## 验收标准

- [ ] 所有核心服务都有单元测试
- [ ] 代码覆盖率 > 80%
- [ ] 所有测试通过

## Commit 信息

```
test: 添加核心分析引擎单元测试

- 添加 CyclomaticComplexityCalculatorTest
- 添加 JavaFileParserTest
- 添加 DependencyAnalyzerTest
- 添加 CodeAnalysisServiceTest
```
