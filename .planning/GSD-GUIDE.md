# GSD 工作流指南

## 快速开始

1. `/gsd:new-project` - 初始化项目（包含研究、需求分析、路线图）
2. `/gsd:plan-phase 1` - 为第一阶段创建详细计划
3. `/gsd:execute-phase 1` - 执行该阶段

## 核心命令

### 项目初始化

**`/gsd:new-project`** - 初始化新项目
- 通过问答理解你的项目愿景
- 可选的领域研究（并行生成 4 个研究 agent）
- 需求定义与范围划分
- 创建路线图和阶段分解

**`/gsd:map-codebase`** - 映射现有代码架构
- 在接手已有项目时使用

### 阶段规划

**`/gsd:discuss-phase <阶段号>`** - 讨论阶段愿景
- 在规划前帮助你表达对这一阶段的设想
- 创建 CONTEXT.md 记录你的愿景和边界

**`/gsd:plan-phase <阶段号>`** - 创建详细执行计划
- 生成 `.planning/phases/XX-计划名/XX-YY-PLAN.md`
- 将阶段分解为具体可执行的任务

### 执行

**`/gsd:execute-phase <阶段号>`** - 执行阶段
- 按波次（wave）分组执行计划
- 验证阶段目标完成度

### 快速模式

**`/gsd:quick`** - 小型临时任务
- 跳过可选 agent，快速执行

**`/gsd:fast [描述]**` - 简单任务
- 无需计划，直接执行（错别字修复、配置更改等）

## 工作流程

```
/gsd:new-project → /gsd:plan-phase → /gsd:execute-phase → 重复
```

## 状态跟踪

所有项目状态保存在 `.planning/` 目录：
- `STATE.md` - 项目当前状态
- `ROADMAP.md` - 路线图和阶段
- `REQUIREMENTS.md` - 需求列表
- `phases/` - 各阶段计划和总结
