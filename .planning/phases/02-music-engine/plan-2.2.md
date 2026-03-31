# Plan 2.2: 实现 MidiGenerator MIDI 生成器

**Wave**: 2  
**优先级**: P0  
**预计耗时**: 4 小时

---

## 目标

实现 MIDI 文件生成器，将音乐参数转换为实际的 MIDI 文件。

## 任务

- [ ] 添加 JFugue 依赖到 pom.xml
- [ ] 创建 `MidiGenerator` 类
- [ ] 实现 `generateMidi(List<ComplexityMetrics>, String outputPath)` 方法
- [ ] 实现批量方法到音轨的映射
- [ ] 支持保存为 MIDI 文件

---

## 验收标准

1. 能够生成有效的 MIDI 文件
2. MIDI 文件可以用标准播放器播放
3. 不同复杂度的代码生成不同的音乐

---

*创建时间：2026-03-31*
