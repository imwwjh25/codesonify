# CodeSonify MVP 测试 Demo

## 快速开始

### 1. 启动应用

```bash
cd /Users/Zhuanz/codesonify
mvn spring-boot:run
```

### 2. 运行测试脚本

```bash
cd demo-test
chmod +x test-demo.sh
./test-demo.sh
```

### 3. 手动测试

#### 方式一：使用前端页面
1. 打开浏览器访问：http://localhost:8080
2. 在"项目路径"输入框输入：`/Users/Zhuanz/codesonify/demo-test/sample-project`
3. 点击"同步分析"按钮
4. 查看分析结果

#### 方式二：使用 curl 命令

```bash
# 1. 代码分析
curl -X POST "http://localhost:8080/api/analysis/analyze?projectPath=/Users/Zhuanz/codesonify/demo-test/sample-project" \
  -H "Content-Type: application/json" | jq

# 2. 生成 HTML 报告（需要分析 ID）
curl "http://localhost:8080/api/report/html?analysisId=<your-analysis-id>" \
  -o report.html

# 3. 生成 Draw.io 依赖图
curl "http://localhost:8080/api/report/diagram?analysisId=<your-analysis-id>" \
  -o dependencies.drawio
```

#### 方式三：使用 Swagger UI

访问 http://localhost:8080/swagger-ui.html 查看完整的 API 文档并在线测试。

---

## 测试项目说明

`sample-project` 目录包含 4 个测试用的 Java 文件：

| 文件 | 复杂度 | 说明 |
|------|--------|------|
| UserService.java | 低 - 中 | 包含简单、中等、复杂三种方法 |
| PaymentService.java | 高 | 包含高复杂度的支付处理方法 |
| Order.java | 极低 | 简单实体类 |
| OrderItem.java | 极低 | 简单实体类 |

---

## 预期输出

### 分析结果示例

```json
{
  "success": true,
  "message": "分析成功",
  "projectAnalysis": {
    "projectName": "sample-project",
    "statistics": {
      "totalClasses": 4,
      "totalMethods": 20,
      "averageCyclomaticComplexity": 5.5,
      "maxCyclomaticComplexity": 15
    }
  }
}
```

### 复杂度分布

- **简单 (1-5)**: UserService.greet(), Order  getters/setters
- **中等 (6-10)**: UserService.getUserLevel(), UserService.processOrder
- **复杂 (11-20)**: PaymentService.processPayment

---

## 功能清单

| 功能 | 端点 | 状态 |
|------|------|------|
| 同步代码分析 | POST /api/analysis/analyze | ✅ |
| 文件分析 | POST /api/analysis/analyze-file | ✅ |
| 获取分析结果 | GET /api/analysis/{id} | ✅ |
| 生成音乐 | POST /api/sonification/generate | ✅ |
| 下载音频 | GET /api/sonification/download/{id} | ✅ |
| HTML 报告 | GET /api/report/html | ✅ |
| Draw.io 图 | GET /api/report/diagram | ✅ |
| 前端页面 | GET / | ✅ |
| API 文档 | GET /swagger-ui.html | ✅ |

---

## 故障排查

### 服务无法启动
```bash
# 检查端口占用
lsof -i :8080

# 清理后重新编译
mvn clean compile
mvn spring-boot:run
```

### 分析失败
- 确保项目路径存在
- 确保路径包含 Java 文件
- 检查日志：`tail -f logs/application.log`

### Redis 连接失败（MVP 版本不需要）
如需启用缓存功能，请先启动 Redis：
```bash
redis-server
```

---

*Last updated: 2026-03-31*
