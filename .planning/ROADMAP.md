# CodeSonify 项目路线图

## 项目概述

**CodeSonify** - 代码复杂度多维度可视化与声音化分析系统

通过视觉 + 听觉多维度呈现代码复杂度：
- 🎵 声音化：将代码复杂度映射为音乐
- 🎨 可视化：生成依赖关系图
- 📊 多维分析：圈复杂度、耦合度、代码行数、嵌套深度

---

## 路线图

### Phase 1: 核心分析引擎

**目标**: 实现 Java 代码的 AST 解析和圈复杂度计算

**Requirements**:
- ANALYZER-01: Java 代码 AST 解析和圈复杂度计算
- ANALYZER-02: 依赖关系图构建和循环依赖检测

**Success Criteria**:
1. 能够解析单个 Java 文件并计算圈复杂度
2. 能够分析整个项目并生成 ClassMetrics 列表
3. 能够构建依赖关系图并检测循环依赖
4. 单元测试覆盖核心计算逻辑

**Estimated Duration**: 2-3 天

---

### Phase 2: 音乐生成引擎

**目标**: 实现复杂度到音乐的映射和 MIDI 文件生成

**Requirements**:
- SONIFICATION-01: 复杂度到音乐的映射（音高、时长、音量、音色）
- SONIFICATION-02: MIDI 文件生成和导出

**Success Criteria**:
1. ComplexityToMusicMapper 正确映射复杂度到 MIDI 参数
2. 能够生成 MIDI 文件并保存到磁盘
3. 生成的音乐能够反映代码复杂度差异
4. 支持多种输出格式（MIDI, WAV）

**Estimated Duration**: 2-3 天

---

### Phase 3: 可视化引擎

**目标**: 实现依赖关系图的可视化和 HTML 报告生成

**Requirements**:
- VISUALIZATION-01: Draw.io 依赖关系图生成
- VISUALIZATION-02: HTML 交互式报告页面

**Success Criteria**:
1. 能够生成 Draw.io 格式的依赖关系图
2. HTML 报告包含复杂度分布图表
3. 支持在浏览器中播放代码音乐
4. 报告可导出为 PDF

**Estimated Duration**: 2-3 天

---

### Phase 4: API 和集成

**目标**: 提供 RESTful API 和异步处理能力

**Requirements**:
- API-01: RESTful API 接口（分析、声音化、下载）
- API-02: Kafka 异步处理支持

**Success Criteria**:
1. 所有 Controller 端点正常工作
2. 支持大项目的异步分析
3. Redis 缓存分析结果
4. 完整的 API 文档（Swagger/OpenAPI）

**Estimated Duration**: 2-3 天

---

## Traceability

| Requirement | Phase | Status |
|-------------|-------|--------|
| ANALYZER-01 | Phase 1 | **Done** |
| ANALYZER-02 | Phase 1 | **Done** |
| SONIFICATION-01 | Phase 2 | Pending |
| SONIFICATION-02 | Phase 2 | Pending |
| VISUALIZATION-01 | Phase 3 | Pending |
| VISUALIZATION-02 | Phase 3 | Pending |
| API-01 | Phase 4 | Pending |
| API-02 | Phase 4 | Pending |

---

*Phase 1 完成于 2026-03-31*
