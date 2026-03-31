---
plan_id: 1.4
plan_number: 4
wave: 2
description: 实现应用服务层
status: pending
---

# Plan 1.4: 实现应用服务层

## 目标

实现应用服务层，编排领域服务并提供代码分析的业务逻辑。

## 任务

### Task 1: 创建 CodeAnalysisService

**文件**: `src/main/java/com/codesonify/application/service/CodeAnalysisService.java`

**工作**:
- 创建 `CodeAnalysisService` 应用服务类
- 注入 `JavaFileParser`, `ClassAnalyzer`, `DependencyAnalyzer`
- 实现 `analyzeProject(String projectPath)` 方法
- 实现 `analyzeFile(String filePath)` 方法
- 返回 `ProjectAnalysis` 对象

### Task 2: 创建 AnalysisResultCache

**文件**: `src/main/java/com/codesonify/application/service/AnalysisResultCache.java`

**工作**:
- 创建 Redis 缓存服务
- 实现 `cacheResult(String key, ProjectAnalysis result, long ttl)` 方法
- 实现 `getCachedResult(String key)` 方法

### Task 3: 创建 CodeAnalysisEvent

**文件**: `src/main/java/com/codesonify/domain/event/CodeAnalysisEvent.java`

**工作**:
- 创建领域事件类
- 实现分析完成事件发布
- 为后续 Kafka 异步处理做准备

## 验收标准

- [ ] 应用服务能够编排领域服务完成分析
- [ ] 支持 Redis 缓存分析结果
- [ ] 事务管理正确

## Commit 信息

```
feat: 实现应用服务层

- 添加 CodeAnalysisService 应用服务
- 添加 AnalysisResultCache 缓存服务
- 添加 CodeAnalysisEvent 领域事件
```
