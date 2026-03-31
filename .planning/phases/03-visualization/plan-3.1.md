# Plan 3.1: 实现 DependencyGraphExporter Draw.io 导出器

**Wave**: 1  
**优先级**: P0  
**预计耗时**: 3 小时

---

## 目标

实现依赖关系图的 Draw.io 格式导出功能。

## 任务

- [ ] 创建 `DependencyGraphExporter` 类
- [ ] 实现 `exportToDrawio(DependencyGraph, String outputPath)` 方法
- [ ] 生成符合 Draw.io 格式的 XML 文件
- [ ] 支持自定义节点样式（根据复杂度着色）

---

## 验收标准

1. 生成的 XML 文件可以在 Draw.io 中打开
2. 节点显示类名和复杂度
3. 边显示依赖方向
4. 高复杂度类使用红色标记

---

*创建时间：2026-03-31*
