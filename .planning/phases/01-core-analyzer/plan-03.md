---
plan_id: 1.3
plan_number: 3
wave: 2
description: 实现依赖关系分析器
status: pending
---

# Plan 1.3: 实现依赖关系分析器

## 目标

实现依赖关系分析服务，能够构建类之间的依赖图并检测循环依赖。

## 任务

### Task 1: 完善 DependencyGraph 实体

**文件**: `src/main/java/com/codesonify/domain/entity/DependencyGraph.java`

**工作**:
- 当前文件已有基础实现
- 添加 `getCyclePaths()` 方法提取完整的循环路径
- 添加 `getDependencyDepth(String className)` 方法计算依赖深度

### Task 2: 创建 DependencyAnalyzer 服务

**文件**: `src/main/java/com/codesonify/domain/service/DependencyAnalyzer.java`

**工作**:
- 创建 `DependencyAnalyzer` 服务类
- 实现 `analyzeDependencies(List<CompilationUnit> units)` 方法
- 提取 import 语句和类型引用
- 构建依赖图并返回 `DependencyGraph`

### Task 3: 创建 CboCalculator 服务

**文件**: `src/main/java/com/codesonify/domain/service/CboCalculator.java`

**工作**:
- 创建 `CboCalculator` 服务类
- 实现 `calculateCBO(ClassOrInterfaceDeclaration classDecl)` 方法
- 计算类之间的耦合度 (Coupling Between Objects)

## 验收标准

- [ ] 能够正确提取类的依赖关系
- [ ] 依赖图能够检测循环依赖
- [ ] CBO 计算准确

## Commit 信息

```
feat: 实现依赖关系分析器

- 完善 DependencyGraph 实体
- 添加 DependencyAnalyzer 服务
- 添加 CboCalculator 服务
```
