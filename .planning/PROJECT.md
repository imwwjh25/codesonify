---
name: CodeSonify 项目
description: 代码复杂度多维度可视化与声音化分析系统
type: project
---

# CodeSonify 项目

## What This Is

CodeSonify 是一个创新性的代码质量分析工具，通过多种感官维度（视觉 + 听觉）呈现代码复杂度。将抽象的代码指标转化为直观的依赖关系图和独特的"代码交响曲"，让开发者从全新角度理解代码质量。

## Core Value

让开发者能够"听见"代码复杂度，通过声音直观识别问题模块。

## Context

- **项目名称**: CodeSonify
- **技术栈**: Java 17 + Spring Boot 3.2 + JavaParser + JGraphT
- **架构**: DDD 四层架构（interface/application/domain/repository）
- **工作流**: GSD (Get Shit Done)

## Key Decisions

| Decision | Rationale | Outcome |
|----------|-----------|---------|
| 使用 JavaParser | Java 生态最成熟的 AST 解析库 | 负责 AST 解析和圈复杂度计算 |
| 使用 JGraphT | Java 图论库，支持依赖分析 | 负责依赖关系图构建和循环检测 |
| DDD 四层架构 | 清晰的职责分离，便于测试和维护 | interface/application/domain/repository |
| MIDI 格式输出 | 轻量级，易于生成和播放 | 使用 JFugue 库生成 MIDI |

## Requirements

### Validated

(None yet — ship to validate)

### Active

- [ ] ANALYZER-01: Java 代码 AST 解析和圈复杂度计算
- [ ] ANALYZER-02: 依赖关系图构建和循环依赖检测
- [ ] SONIFICATION-01: 复杂度到音乐的映射（音高、时长、音量、音色）
- [ ] SONIFICATION-02: MIDI 文件生成和导出
- [ ] VISUALIZATION-01: Draw.io 依赖关系图生成
- [ ] VISUALIZATION-02: HTML 交互式报告页面
- [ ] API-01: RESTful API 接口（分析、声音化、下载）
- [ ] API-02: Kafka 异步处理支持

### Out of Scope

- 多语言支持（Python、JavaScript 等）— 第一期仅支持 Java
- VR/AR 体验 — 长期愿景，当前不实现
- 游戏化功能 — 长期愿景

---
*Last updated: 2026-03-31 - 项目初始化*
