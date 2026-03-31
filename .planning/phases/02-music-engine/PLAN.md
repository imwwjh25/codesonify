# Phase 2: 音乐生成引擎

**目标**: 实现复杂度到音乐的映射和 MIDI 文件生成

**周期**: 2026-03-31 ~ 2026-04-02 (预计 2-3 天)

---

## Requirements

| ID | 名称 | 描述 |
|----|------|------|
| SONIFICATION-01 | 复杂度到音乐的映射 | 将圈复杂度、代码行数、嵌套深度等指标映射为 MIDI 参数（音高、时长、音量、音色） |
| SONIFICATION-02 | MIDI 文件生成和导出 | 生成 MIDI 文件并支持导出为 WAV/MP3 格式 |

---

## Success Criteria

1. ✅ `ComplexityToMusicMapper` 正确映射复杂度到 MIDI 参数
2. ✅ `MidiGenerator` 能够生成 MIDI 文件并保存到磁盘
3. ✅ 生成的音乐能够反映代码复杂度差异
4. ✅ 支持多种输出格式（MIDI, WAV）
5. ✅ 单元测试覆盖核心映射逻辑和 MIDI 生成

---

## Waves

| Wave | 计划 | 描述 | 状态 |
|------|------|------|------|
| Wave 1 | 2.1 | 完善 ComplexityToMusicMapper 映射逻辑 | pending |
| Wave 2 | 2.2 | 实现 MidiGenerator MIDI 生成器 | pending |
| Wave 3 | 2.3 | 编写单元测试 | pending |

---

## Dependencies

- **前置 Phase**: Phase 1 (核心分析引擎) ✅ 已完成
- **外部依赖**: JFugue MIDI 库 (已添加到 pom.xml)
- **依赖模块**: domain.entity, domain.service

---

## 交付成果

**领域服务**:
- `ComplexityToMusicMapper` - 复杂度到音乐参数映射
- `MidiGenerator` - MIDI 文件生成器

**应用服务**:
- `CodeSonificationService` - 代码声音化应用服务

**单元测试**:
- `ComplexityToMusicMapperTest` - 映射逻辑测试
- `MidiGeneratorTest` - MIDI 生成测试

---

*创建时间：2026-03-31*
