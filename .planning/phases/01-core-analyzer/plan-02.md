---
plan_id: 1.2
plan_number: 2
wave: 1
description: 实现圈复杂度计算器
status: pending
---

# Plan 1.2: 实现圈复杂度计算器

## 目标

实现圈复杂度计算服务，能够分析方法的控制流复杂度。

## 任务

### Task 1: 完善 CyclomaticComplexityCalculator

**文件**: `src/main/java/com/codesonify/domain/service/CyclomaticComplexityCalculator.java`

**工作**:
- 当前文件已存在基础实现
- 添加 `calculateForClass(CompilationUnit cu)` 方法
- 返回类中所有方法的复杂度列表

### Task 2: 创建 MethodAnalyzer 服务

**文件**: `src/main/java/com/codesonify/domain/service/MethodAnalyzer.java`

**工作**:
- 创建 `MethodAnalyzer` 服务类
- 实现 `analyzeMethod(MethodDeclaration method)` 方法
- 收集方法的所有指标（复杂度、行数、参数等）
- 返回 `ComplexityMetrics` 对象

### Task 3: 创建 ClassAnalyzer 服务

**文件**: `src/main/java/com/codesonify/domain/service/ClassAnalyzer.java`

**工作**:
- 创建 `ClassAnalyzer` 服务类
- 实现 `analyzeClass(CompilationUnit cu)` 方法
- 聚合类中所有方法的指标
- 返回 `ClassMetrics` 对象

## 验收标准

- [ ] 圈复杂度计算正确（通过已知复杂度的测试用例验证）
- [ ] 能够分析整个类的复杂度
- [ ] 单元测试覆盖所有决策点类型

## 测试用例

```java
// 简单方法 (CC=1)
public int add(int a, int b) { return a + b; }

// 中等复杂度 (CC=3)
public int max(int a, int b, int c) {
    if (a > b) {
        return a > c ? a : c;
    }
    return b > c ? b : c;
}

// 高复杂度 (CC=5+)
public void process(List<String> items) {
    for (String item : items) {
        if (item != null && item.length() > 0) {
            if (item.startsWith("A") || item.startsWith("B")) {
                // ...
            }
        }
    }
}
```

## Commit 信息

```
feat: 实现圈复杂度计算器

- 完善 CyclomaticComplexityCalculator
- 添加 MethodAnalyzer 和 ClassAnalyzer
- 支持完整的代码指标收集
```
