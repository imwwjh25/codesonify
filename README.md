# CodeSonify

**代码复杂度多维度可视化与声音化分析系统**

将代码的复杂度指标转换为音乐，通过听觉方式感知代码质量和复杂度。

---

## 快速开始

### 启动应用

```bash
mvn spring-boot:run
```

启动后访问：
- **前端页面**: http://localhost:8080/index.html
- **API 文档**: http://localhost:8080/swagger-ui.html

### 测试 Demo

```bash
# 使用内置的示例项目进行测试
curl -X POST "http://localhost:8080/api/analysis/analyze?projectPath=/Users/Zhuanz/codesonify/demo-test/sample-project"
```

---

## 功能特性

### 代码分析
- 圈复杂度分析
- 嵌套深度统计
- 代码行数统计
- 方法类型识别

### 音乐生成
- 将代码指标映射为音乐参数
- 支持 MIDI 格式生成
- 支持文件下载

### 可视化报告
- 复杂度分布统计
- HTML 报告生成（开发中）

---

## 技术栈

| 层次 | 技术 |
|------|------|
| 后端 | Spring Boot 3.2.0 + Java 21 |
| 解析 | JavaParser + JGraphT |
| 音乐 | javax.sound.midi |
| 缓存 | 内存缓存 (MVP) |

---

## 文档

- [MVP 开发总结与踩坑记录](./docs/MVP-总结与踩坑记录.md)
- [快速启动指南](./docs/快速启动指南.md)
- [API 文档](http://localhost:8080/swagger-ui.html)

---

## 项目结构

```
codesonify/
├── src/main/java/com/codesonify/
│   ├── interfaces/      # Controller/DTO
│   ├── application/     # 业务服务
│   ├── domain/          # 实体/领域服务
│   └── repository/      # 基础设施
├── src/main/resources/
│   ├── application.yml
│   └── static/index.html
├── demo-test/           # 测试示例
└── docs/                # 文档
```

---

## 运行测试

```bash
mvn test
```

---

## License

MIT
