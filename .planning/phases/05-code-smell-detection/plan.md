# Phase 5: 代码异味检测

**优先级**: 高
**预估工作量**: 3-4 天

---

## 阶段目标

实现代码异味检测功能，能够识别和分析常见代码异味，为代码重构提供指导。

---

## Success Criteria

1. 能够检测至少 5 种常见代码异味
2. 生成详细的代码异味报告
3. 支持通过 API 获取检测结果
4. 在 HTML 报告中展示代码异味信息
5. 单元测试覆盖核心检测逻辑

---

## 需求规格

### CODESMELL-01: 长方法检测
- 检测方法行数超过阈值（默认 50 行）的方法
- 识别过深的嵌套层级（默认 4 层）
- 计算方法的圈复杂度
- 支持自定义阈值

### CODESMELL-02: 大类检测
- 检测类行数超过阈值（默认 300 行）的类
- 检测方法数量超过阈值（默认 15 个）的类
- 检测字段数量超过阈值（默认 20 个）的类
- 支持自定义阈值

### CODESMELL-03: 重复代码检测
- 检测相似的代码片段
- 计算代码重复率
- 标记重复代码的位置

### CODESMELL-04: 过多参数检测
- 检测参数数量超过阈值（默认 5 个）的方法
- 支持自定义阈值

### CODESMELL-05: 过度耦合检测
- 基于耦合度（CBO）检测过度耦合的类
- 检测缺少适当封装的字段

---

## DDD 架构设计

### 领域层 (domain)

**实体 (entity)**:
- `CodeSmell` - 代码异味实体
  - id: UUID
  - type: SmellType (枚举)
  - severity: Severity (枚举)
  - className: String
  - methodName: String (可选)
  - lineNumber: int
  - message: String
  - suggestion: String

**值对象 (valueobject)**:
- `SmellType` - 代码异味类型枚举
  - LONG_METHOD
  - LARGE_CLASS
  - DUPLICATE_CODE
  - LONG_PARAMETER_LIST
  - HIGH_COUPLING
  - GOD_CLASS
  - FEATURE_ENVY
- `Severity` - 严重程度枚举
  - CRITICAL
  - HIGH
  - MEDIUM
  - LOW

**领域服务 (service)**:
- `CodeSmellDetector` - 代码异味检测器
  - detectCodeSmells(List<ClassMetrics>): List<CodeSmell>
  - detectLongMethod(MethodMetrics): Optional<CodeSmell>
  - detectLargeClass(ClassMetrics): Optional<CodeSmell>
  - detectDuplicateCode(List<ClassMetrics>): List<CodeSmell>
  - detectLongParameterList(MethodMetrics): Optional<CodeSmell>
  - detectHighCoupling(ClassMetrics): Optional<CodeSmell>
- `DuplicateCodeAnalyzer` - 重复代码分析器
  - analyzeDuplicates(List<ClassMetrics>): List<DuplicateCodeBlock>
- `CodeSmellAggregator` - 代码异味聚合器
  - aggregateByType(List<CodeSmell>): Map<SmellType, List<CodeSmell>>
  - aggregateBySeverity(List<CodeSmell>): Map<Severity, List<CodeSmell>>
  - calculateScore(List<CodeSmell>): double

### 应用层 (application)

**服务 (service)**:
- `CodeSmellAnalysisService` - 代码异味分析应用服务
  - analyzeProject(String projectPath): CodeSmellAnalysisResult
  - analyzeClass(ClassMetrics): List<CodeSmell>
  - generateReport(CodeSmellAnalysisResult): CodeSmellReport

### 接口层 (interfaces)

**DTO/Request/Response**:
- `CodeSmellDetectionRequest` - 检测请求
- `CodeSmellDetectionResponse` - 检测响应
- `CodeSmellDTO` - 代码异味 DTO
- `CodeSmellAnalysisResultDTO` - 分析结果 DTO

**控制器 (controller)**:
- `CodeSmellController` - 代码异味控制器
  - POST /api/codesmells/analyze - 分析项目
  - GET /api/codesmells/{projectId} - 获取检测结果
  - GET /api/codesmells/{projectId}/report - 获取报告

---

## 执行计划

### Wave 1: 领域层实现

**任务 1.1**: 创建代码异味实体和值对象
- 创建 `CodeSmell` 实体
- 创建 `SmellType` 枚举
- 创建 `Severity` 枚举

**任务 1.2**: 实现 `CodeSmellDetector` 领域服务
- 实现 `detectLongMethod` 方法
- 实现 `detectLargeClass` 方法
- 实现 `detectLongParameterList` 方法
- 实现 `detectHighCoupling` 方法

**任务 1.3**: 实现 `DuplicateCodeAnalyzer` 领域服务
- 实现重复代码检测算法
- 实现 `analyzeDuplicates` 方法

**任务 1.4**: 实现 `CodeSmellAggregator` 领域服务
- 实现按类型聚合
- 实现按严重程度聚合
- 实现评分算法

### Wave 2: 应用层和接口层实现

**任务 2.1**: 创建 DTO/Request/Response
- 创建 `CodeSmellDetectionRequest`
- 创建 `CodeSmellDetectionResponse`
- 创建 `CodeSmellDTO`
- 创建 `CodeSmellAnalysisResultDTO`

**任务 2.2**: 实现 `CodeSmellAnalysisService` 应用服务
- 实现 `analyzeProject` 方法
- 实现 `analyzeClass` 方法
- 实现 `generateReport` 方法

**任务 2.3**: 实现 `CodeSmellController` 控制器
- 实现 `/api/codesmells/analyze` 端点
- 实现 `/api/codesmells/{projectId}` 端点
- 实现 `/api/codesmells/{projectId}/report` 端点

### Wave 3: 集成和增强

**任务 3.1**: 集成到 HTML 报告
- 在 HTML 报告中添加代码异味标签页
- 显示代码异味统计图表
- 显示代码异味详情表格

**任务 3.2**: 实现 Swagger API 文档
- 添加 API 注解
- 配置 Swagger 文档

**任务 3.3**: 编写单元测试
- `CodeSmellDetectorTest`
- `DuplicateCodeAnalyzerTest`
- `CodeSmellAggregatorTest`
- `CodeSmellAnalysisServiceTest`
- `CodeSmellControllerTest`

---

## 配置

### 配置参数

```yaml
codesonify:
  codesmell:
    thresholds:
      long-method:
        max-lines: 50
        max-nesting-depth: 4
      large-class:
        max-lines: 300
        max-methods: 15
        max-fields: 20
      long-parameter-list:
        max-parameters: 5
      high-coupling:
        max-cbo: 10
    duplicate-code:
      min-block-size: 6
      similarity-threshold: 0.9
```

---

## 依赖

无需新增外部依赖，复用现有：
- JavaParser (已有)
- JGraphT (已有)

---

## 验收标准

- [ ] 能够检测至少 5 种代码异味
- [ ] 生成详细的代码异味报告
- [ ] API 端点正常工作
- [ ] HTML 报告包含代码异味信息
- [ ] 单元测试覆盖率 >= 80%
- [ ] 所有测试通过

---

## 风险与缓解

**风险 1**: 重复代码检测算法可能性能较差
- **缓解**: 使用 AST 相似度检测，限制分析范围

**风险 2**: 阈值配置不适用所有项目
- **缓解**: 支持自定义配置，提供默认值

**风险 3**: 检测结果可能产生误报
- **缓解**: 提供严重程度分级，用户可自定义

---

*创建时间：2026-03-31*