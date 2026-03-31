# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 技术栈

- **主要语言**: Java
- **核心框架**: Spring Boot, Spring, Redis, MySQL, MyBatis-Plus, Kafka
- **其他语言**: 如需使用其他语言必须先获得用户许可

## 架构设计

采用领域驱动设计 (DDD) 标准四层架构:

```
interface    → 用户接口层 (Controller/DTO/Request/Response)
application  → 应用服务层 (业务编排/事务管理)
domain       → 领域层 (实体/值对象/领域服务/领域事件)
repository   → 基础设施层 (数据持久化/外部系统调用)
```

## Git 工作流

- 必须在 Git 分支中编码
- 每个功能独立分支开发
- 测试通过后再合并到 master 分支
- 禁止直接在 master 分支提交代码

## 编码规范

- **命名**: 标准驼峰式命名，见名知意
- **语言**: 注释和文档均使用中文
- **包路径**: 按照 DDD 四层架构组织代码

## GSD 工作流

本项目采用 GSD (Get Shit Done) 规格驱动开发工作流：

- **入口命令**: `/gsd:quick` (小修), `/gsd:fast` (快速任务), `/gsd:execute-phase` (计划任务)
- **状态跟踪**: 所有开发状态记录在 `.planning/STATE.md`
- **工作流程**: 需求 → 规格 → 计划 → 执行 → 验证
- **配置文件**: `.planning/config.json`, `.planning/ROADMAP.md`

直接修改代码前应通过 GSD 命令启动工作流，确保规划与执行同步。
