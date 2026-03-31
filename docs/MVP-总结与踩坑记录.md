# CodeSonify MVP 原型开发总结与踩坑记录

## 项目概述

**CodeSonify** - 代码复杂度多维度可视化与声音化分析系统

将代码的复杂度指标（圈复杂度、嵌套深度、代码行数等）转换为音乐，通过听觉方式感知代码质量和复杂度。

### 技术栈

| 层次 | 技术选型 |
|------|----------|
| 后端框架 | Spring Boot 3.2.0 + Java 21 |
| 数据解析 | JavaParser (AST 解析) |
| 图分析 | JGraphT (依赖图分析) |
| 音乐生成 | javax.sound.midi (MIDI 生成) |
| 数据存储 | 内存缓存 (MVP) / Redis (生产) |
| API 文档 | SpringDoc OpenAPI 3 |

### DDD 四层架构

```
interface    → Controller/DTO/Request/Response
application  → 业务编排/事务管理
domain       → 实体/值对象/领域服务
repository   → 数据持久化/外部系统调用
```

---

## MVP 功能完成情况

### 已实现功能

| 功能模块 | 状态 | 说明 |
|----------|------|------|
| 代码分析（同步） | ✅ | 分析 Java 文件的复杂度指标 |
| 分析结果缓存 | ✅ | 内存缓存 + TTL 过期 |
| 音乐生成 | ✅ | 生成 MIDI 格式音乐文件 |
| MIDI 下载 | ✅ | 支持下载后用播放器播放 |
| HTML 报告 | 🔄 | 基础框架已搭建 |
| 异步分析（Kafka）| ⏸️ | MVP 阶段注释，保留代码 |

### 支持的分析指标

- 圈复杂度 (Cyclomatic Complexity)
- 嵌套深度 (Nesting Depth)
- 代码行数 (Lines of Code)
- 方法类型识别 (Getter/Setter/Constructor/业务方法)
- 复杂度分布统计

### 音乐映射规则

| 代码指标 | 音乐参数 | 映射规则 |
|----------|----------|----------|
| 圈复杂度 | 音高 (Pitch) | 复杂度越高，音高越高 (60-84) |
| 嵌套深度 | 音量 (Velocity) | 嵌套越深，音量越大 |
| 代码行数 | 时长 (Duration) | 代码越长，音符时值越长 |
| 方法类型 | 乐器 (Instrument) | 不同方法类型使用不同乐器 |

---

## 踩坑记录与解决方案

### 坑 1: JavaParser 解析后无法获取类信息

**问题现象**：
```json
{
  "totalClasses": 0,
  "totalMethods": 0,
  "averageCyclomaticComplexity": 0
}
```

**排查过程**：
1. 日志显示 `分析了 0 个类`
2. 文件确实被读取了
3. `CompilationUnit` 解析成功但没有类

**根本原因**：
```java
// 错误的写法 ❌
try (FileInputStream fis = new FileInputStream(file)) {
    CompilationUnit cu = StaticJavaParser.parse(fis);  // 使用 InputStream
    return cu;
}
```

`StaticJavaParser.parse(InputStream)` 无法保留文件路径信息，导致后续 `cu.getStorage()` 返回空，`ClassAnalyzer` 无法处理。

**解决方案**：
```java
// 正确的写法 ✅
try (FileInputStream fis = new FileInputStream(file)) {
    CompilationUnit cu = StaticJavaParser.parse(file);  // 直接传 File 对象
    return cu;
}
```

**经验教训**：使用 JavaParser 时，优先使用 `parse(File)` 或 `parse(Path)` 而不是 `parse(InputStream)`，除非有特殊需求。

---

### 坑 2: 分析结果不存在 - Redis 依赖问题

**问题现象**：
```
分析功能正常使用，但生成音乐时报错：
"分析结果不存在：ec47ee0a-218e-4186-85ea-1299bd8c1c15"
```

**排查过程**：
1. 分析成功返回分析 ID
2. 调用音乐生成接口时提示分析结果不存在
3. 日志显示 `getCachedAnalysis` 返回 null

**根本原因**：
- `CacheService` 依赖 `RedisTemplate`
- 本地测试没有启动 Redis 服务
- Redis 连接失败导致缓存写入实际没有成功

**解决方案**：
改用内存缓存实现（适合 MVP 测试）：

```java
@Service
public class CacheService {
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    
    public CacheService() {
        // 每分钟清理一次过期缓存
        scheduler.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);
    }
    
    private static class CacheEntry {
        final ProjectAnalysis analysis;
        final long expireTime;
        
        CacheEntry(ProjectAnalysis analysis, long expireTime) {
            this.analysis = analysis;
            this.expireTime = expireTime;
        }
    }
}
```

**经验教训**：
1. MVP 阶段尽量减少外部依赖（Redis/Kafka 等）
2. 使用接口抽象，方便后续切换实现
3. 缓存服务应该对外暴露统一的 TTL 控制

---

### 坑 3: 浏览器无法播放 MIDI 音频

**问题现象**：
```
用户问题："为什么生成的音乐是 0 秒的时间长度？"
```

**排查过程**：
1. 后端返回 `duration: 40.0` 秒，数据正确
2. MIDI 文件生成正常，可以用本地播放器播放
3. 前端 `<audio>` 控件显示 00:00，无法播放

**根本原因**：
```javascript
// 错误的写法 ❌
audioPlayer.src = 'data:audio/midi;base64,' + midiBase64;
```

**MIDI 不是音频格式**，而是乐器指令格式：
- MIDI 文件包含的是 "演奏 C4 音符，音量 80，持续 2 拍" 这样的指令
- 浏览器 `<audio>` 元素支持 WAV/MP3/OGG 等音频格式
- 浏览器不支持 `audio/midi` MIME 类型的 Data URL

**解决方案对比**：

| 方案 | 优点 | 缺点 |
|------|------|------|
| MIDI→WAV 转换 | 浏览器直接播放 | Java 没有内置渲染器，实现复杂 |
| 前端 MIDI 播放器 | 体验好 | 需要引入第三方库 (如 Tone.js) |
| 下载 + 在线播放器 | 简单可靠 | 需要额外步骤 |

**MVP 选择方案**：下载 MIDI 文件 + 推荐在线播放器

```javascript
// 前端改进
function downloadMidi() {
    const link = document.createElement('a');
    link.href = 'data:audio/midi;base64,' + window.currentMidiData;
    link.download = 'codesonify-' + analysisId + '.mid';
    link.click();
}
```

```html
<!-- 引导用户使用在线播放器 -->
<div class="btn-group">
    <button class="btn btn-primary" onclick="downloadMidi()">📥 下载 MIDI 文件</button>
    <a href="https://onlinesequencer.com/" target="_blank" class="btn btn-secondary">
        🎹 在在线播放器中打开
    </a>
</div>
```

**经验教训**：
1. 音频格式的选择要提前调研浏览器支持情况
2. MIDI 适合专业音乐制作，不适合 Web 直接播放
3. MVP 优先选择简单可靠的方案

---

### 坑 4: 分析结果全部为 0

**问题现象**：
```json
{
  "statistics": {
    "totalClasses": 0,
    "totalMethods": 0,
    "maxCyclomaticComplexity": 0
  }
}
```

**排查过程**：
1. Sample Project 明明有 4 个 Java 文件
2. `JavaFileParser` 日志显示 `找到 4 个 Java 文件`
3. 但 `ClassAnalyzer` 始终分析不到类

**调试日志**：
```
处理编译单元：
分析了 0 个类
```

**根本原因**：
与坑 1 相同，是 `StaticJavaParser.parse()` 的参数问题。

**解决方案**：
```java
// 修改 JavaFileParser.parseFile()
CompilationUnit cu = StaticJavaParser.parse(file);  // 使用 File 参数
```

**验证结果**：
```
处理编译单元：/var/folders/.../UserService.java
发现类：UserService
分析了 4 个类，共 20 个方法
```

---

### 坑 5: 异步分析 Kafka 代码影响 MVP 测试

**问题**：
- Kafka 异步分析代码耦合在 Controller 中
- 测试环境没有 Kafka，启动报错

**解决方案**：
注释掉 Kafka 相关代码（不是删除），保留 TODO 标记：

```java
// TODO: Kafka 异步支持 - 恢复时取消注释
// private final AnalysisProducer analysisProducer;
// private final AnalysisConsumer analysisConsumer;

/*
@PostMapping("/analyze/async")
public ResponseEntity<AnalysisResponse> analyzeProjectAsync(...) {
    // 异步实现
}
*/
```

**经验教训**：
1. 功能开关用 Feature Flag 比注释更好
2. 异步代码应该独立封装，不要和同步代码混在一起
3. 注释代码要写明 "为什么注释" 和 "如何恢复"

---

## 有价值的技术决策

### 1. 选择 MIDI 而不是 WAV/MP3

**决策背景**：
音乐生成需要将代码指标映射到音乐参数。

**决策过程**：
- WAV/MP3：音频格式，浏览器支持好，但生成复杂
- MIDI：指令格式，生成简单，但需要额外播放器

**最终决策**：MVP 阶段用 MIDI，理由：
1. `javax.sound.midi` 是 JDK 内置库，无需依赖
2. 音乐生成逻辑简单清晰
3. 后续可升级到 WAV（不影响核心逻辑）

### 2. 内存缓存替代 Redis

**决策背景**：
MVP 测试阶段，减少外部依赖。

**设计要点**：
```java
// TTL 过期机制
private static final long DEFAULT_TTL = 3600; // 1 小时

// 定时清理
scheduler.scheduleAtFixedRate(this::cleanupExpired, 1, 1, TimeUnit.MINUTES);

// ConcurrentHashMap 保证线程安全
private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
```

### 3. 复杂度到音乐的映射策略

**映射表**：
```java
public int complexityToPitch(int complexity) {
    // 1-5   → 60-64 (C4-E4)
    // 6-10  → 65-72 (F4-C5)
    // 11-20 → 73-84 (C#5-C6)
    return Math.min(84, 59 + complexity);
}

public int depthToVelocity(int depth) {
    // 1 层  → 60  (弱)
    // 2-3 层 → 80  (中)
    // 4+层 → 100 (强)
    return Math.min(127, 40 + depth * 20);
}
```

---

## 项目结构总结

```
codesonify/
├── src/main/java/com/codesonify/
│   ├── interfaces/           # 接口层
│   │   ├── controller/
│   │   │   ├── AnalysisController.java      # 分析接口
│   │   │   ├── SonificationController.java  # 音乐生成接口
│   │   │   └── ReportController.java        # 报告生成接口
│   │   └── dto/
│   │       ├── AnalysisResponse.java
│   │       └── SonificationResponse.java
│   ├── application/          # 应用层
│   │   └── service/
│   │       ├── CodeAnalysisService.java
│   │       └── CodeSonificationService.java
│   ├── domain/               # 领域层
│   │   ├── entity/
│   │   │   ├── ProjectAnalysis.java
│   │   │   ├── ClassMetrics.java
│   │   │   └── ComplexityMetrics.java
│   │   └── service/
│   │       ├── JavaFileParser.java         # Java 文件解析
│   │       ├── ClassAnalyzer.java          # 类复杂度分析
│   │       ├── ComplexityToMusicMapper.java # 复杂度→音乐映射
│   │       └── MidiGenerator.java          # MIDI 生成
│   └── repository/           # 基础设施层
│       └── CacheService.java               # 缓存服务
├── src/main/resources/
│   ├── application.yml       # 配置文件
│   └── static/
│       └── index.html        # 前端页面
└── demo-test/sample-project/ # 测试示例项目
```

---

## 测试结果

### 单元测试
```
Tests run: 74, Failures: 0, Errors: 0, Skipped: 0
```

### 功能测试

| 测试场景 | 输入 | 输出 | 状态 |
|----------|------|------|------|
| 代码分析 | sample-project (4 个类) | 20 个方法，平均复杂度 2.2 | ✅ |
| 音乐生成 | 分析结果 | MIDI 文件，40 秒，160 个音符 | ✅ |
| MIDI 下载 | 分析 ID | .mid 文件下载 | ✅ |
| 缓存服务 | TTL 过期 | 自动清理 | ✅ |

---

## 后续优化方向

### 短期（下一阶段）

1. **WAV 格式支持**
   - 使用 JLayer/MP3SPI 等库进行格式转换
   - 前端直接播放音频

2. **恢复 Kafka 异步支持**
   - 取消注释异步代码
   - 恢复 Redis 依赖

3. **完善报告生成**
   - HTML 报告模板
   - 复杂度趋势图

### 中期

1. **支持更多语言**
   - JavaScript/TypeScript (使用 tree-sitter)
   - Python (使用 ast 模块)
   - Go (使用 go/ast)

2. **音乐生成优化**
   - 更多音乐风格（古典、电子、爵士）
   - 和弦进行支持
   - 节奏模式变化

3. **可视化增强**
   - 依赖图可视化
   - 复杂度热力图

---

## 经验总结

### 技术层面

1. **MVP 原则**：先让系统跑起来，再考虑优化
   - 内存缓存替代 Redis
   - 注释 Kafka 异步代码
   - MIDI 下载替代在线播放

2. **调试技巧**：
   - 日志要详细（尤其是数据流）
   - 单元测试覆盖核心逻辑
   - 准备测试数据（sample project）

3. **架构设计**：
   - DDD 分层清晰，便于维护
   - 接口抽象便于替换实现
   - 配置外置便于部署

### 踩坑教训

1. **文件解析**：使用 `parse(File)` 而不是 `parse(InputStream)`
2. **浏览器音频**：MIDI 不是音频格式，注意调研
3. **外部依赖**：MVP 尽量减少依赖，降低测试门槛
4. **注释代码**：写清楚 "为什么注释" 和 "如何恢复"

---

## 参考资料

- [JavaParser 官方文档](https://javaparser.org/)
- [Java Sound MIDI 教程](https://docs.oracle.com/javase/tutorial/sound/MIDI.html)
- [Spring Boot 3.2 文档](https://docs.spring.io/spring-boot/docs/3.2.0/reference/htmlsingle/)
- [onlinesequencer.com](https://onlinesequencer.com/) - 在线 MIDI 播放器

---

*文档创建时间：2026-03-31*
*项目版本：1.0.0-SNAPSHOT*
