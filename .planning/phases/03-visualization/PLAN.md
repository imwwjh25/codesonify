# Phase 3: 可视化引擎

**目标**: 实现依赖关系图的可视化和 HTML 报告生成

**周期**: 2026-03-31 ~ 2026-04-03 (预计 2-3 天)

---

## Requirements

| ID | 名称 | 描述 |
|----|------|------|
| VISUALIZATION-01 | Draw.io 依赖关系图生成 | 将类之间的依赖关系导出为 Draw.io (diagram.io) 格式 |
| VISUALIZATION-02 | HTML 交互式报告页面 | 生成包含复杂度分布图表和代码音乐的 HTML 报告 |

---

## Success Criteria

1. ✅ 能够生成 Draw.io 格式的依赖关系图
2. ✅ HTML 报告包含复杂度分布图表
3. ✅ 支持在浏览器中播放代码音乐（MIDI 播放）
4. ✅ 报告可导出为 PDF

---

## Waves

| Wave | 计划 | 描述 | 状态 |
|------|------|------|------|
| Wave 1 | 3.1 | 实现 DependencyGraphExporter Draw.io 导出器 | pending |
| Wave 2 | 3.2 | 实现 HtmlReportGenerator HTML 报告生成器 | pending |
| Wave 3 | 3.3 | 编写单元测试 | pending |

---

## Dependencies

- **前置 Phase**: Phase 1 (核心分析引擎) ✅ 已完成
- **外部依赖**: 无（使用纯 Java 实现）
- **依赖模块**: domain.entity, domain.service, application.service

---

## 交付成果

**领域服务**:
- `DependencyGraphExporter` - 依赖图导出器（支持 Draw.io 格式）

**应用服务**:
- `ReportGenerationService` - 报告生成应用服务

**单元测试**:
- `DependencyGraphExporterTest` - 导出逻辑测试
- `ReportGenerationServiceTest` - 报告生成测试

---

*创建时间：2026-03-31*
