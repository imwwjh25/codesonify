# 经验教训总结 - Phase 1 编译错误

## 事件概述

**发生时间**: 2026-03-31  
**问题**: Phase 1 完成后发现代码存在大量编译错误，无法通过 `mvn compile`  
**影响**: 需要额外时间修复本应避免的低级错误

---

## 问题清单

### 1. 包名使用 Java 关键字 ❌

**问题描述**:  
使用 `com.codesonify.interface.*` 作为包名，但 `interface` 是 Java 关键字。

**错误现象**:
```
ERROR: 需要<标识符>
ERROR: 需要 class、interface、enum 或 record
```

**正确做法**:
- 使用 `interfaces`（复数形式）或 `infrastructure`
- 避免使用任何 Java 关键字作为包名：`interface`, `class`, `enum`, `package`, `import` 等

**修复方案**:
```bash
# 重命名目录
mv src/main/java/com/codesonify/interface src/main/java/com/codesonify/interfaces

# 更新所有文件中的包声明和 import
```

---

### 2. Lombok 注解导入不完整 ❌

**问题描述**:  
使用了 `@Builder` 注解但未导入 `lombok.Builder`。

**错误现象**:
```
ERROR: 找不到符号
  符号: 方法 builder()
  位置：类 Xxx
```

**正确做法**:
- 使用 `@Data` + `@Builder` 时，确保两个注解都有导入
- 或者直接使用 `@Data`（包含 `@Getter`, `@Setter`, `@EqualsAndHashCode`, `@ToString`, `@RequiredArgsConstructor`）

**修复方案**:
```java
// 添加缺失的导入
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Xxx { ... }
```

---

### 3. JavaParser API 版本差异 ❌

**问题描述**:  
代码基于旧版本 JavaParser 编写，但项目使用 3.25.4 版本，API 有变更。

**具体差异**:

| 旧 API | 新 API (3.25.4) |
|--------|----------------|
| `ForEachStmt` (简单类名) | `com.github.javaparser.ast.stmt.ForEachStmt` (全限定名) |
| `method.getSignature()` 返回 `String` | 返回 `Signature` 对象，需调用 `.asString()` |
| `type.asClassOrInterfaceType().ifPresent(...)` | `if (type.isClassOrInterfaceType()) { type.asClassOrInterfaceType()... }` |
| `method.isConstructor()` | `method.getNameAsString().equals("<init>")` |

**错误现象**:
```
ERROR: 找不到符号
  符号：类 ForeachStmt
ERROR: 不兼容的类型
  符号：方法 getSignature()
ERROR: 方法引用无效
  符号：方法 isConstructor()
```

**正确做法**:
- 在开始编码前，先确认依赖库的版本
- 查阅对应版本的官方文档或 API 参考
- 对于不确定的 API，先写简单的测试代码验证

**修复方案**:
```java
// ❌ 错误写法
param.getType().asClassOrInterfaceType().ifPresent(type -> {
    dependencies.add(type.getNameAsString());
});

// ✅ 正确写法
var type = param.getType();
if (type.isClassOrInterfaceType()) {
    dependencies.add(type.asClassOrInterfaceType().getNameAsString());
}
```

---

### 4. Maven 编译配置与 Java 版本不匹配 ❌

**问题描述**:  
系统默认使用 Java 21，但 Lombok 1.18.34 与 Java 21 不完全兼容。

**错误现象**:
```
ERROR: Fatal error compiling
java.lang.ExceptionInInitializerError: com.sun.tools.javac.code.TypeTag :: UNKNOWN
```

**正确做法**:
- 在 `pom.xml` 中明确指定 Java 版本
- 使用与环境匹配的 Lombok 版本
- 配置 Maven 编译器插件使用正确的 JDK

**修复方案**:
```xml
<properties>
    <java.version>17</java.version>
    <lombok.version>1.18.34</lombok.version>
</properties>

<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <source>17</source>
                <target>17</target>
                <annotationProcessorPaths>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
    </plugins>
</build>
```

**编译命令**:
```bash
# 使用 Java 17 编译
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
mvn clean compile
```

---

### 5. 测试用例依赖未经验证的 API ❌

**问题描述**:  
测试用例使用 `cu.getPrimaryTypeName()`，但该 API 在 JavaParser 3.25.4 中行为不同。

**错误现象**:
```
testParseFile: 应该有主类名 ==> expected: <true> but was: <false>
```

**正确做法**:
- 编写测试前先验证 API 行为
- 使用更稳定的 API 替代方案

**修复方案**:
```java
// ❌ 不稳定写法
assertTrue(cu.getPrimaryTypeName().isPresent());
assertEquals("HelloWorld", cu.getPrimaryTypeName().get());

// ✅ 稳定写法
assertFalse(cu.getTypes().isEmpty());
assertEquals("HelloWorld", cu.getTypes().get(0).getNameAsString());
```

---

## 预防措施

### 编码前检查清单

- [ ] 确认项目依赖版本（JavaParser, JGraphT, Lombok 等）
- [ ] 查阅依赖库的官方文档/API 参考
- [ ] 确认 Java 版本与 Lombok 版本兼容
- [ ] 编写简单的 API 验证代码

### 编码中检查清单

- [ ] 避免使用 Java 关键字作为包名/类名
- [ ] 所有 Lombok 注解都要有对应的 import
- [ ] 使用 IDE 的代码检查功能（红色波浪线）
- [ ] 对于不确定的 API，先写单元测试验证

### 提交前检查清单

- [ ] `mvn clean compile` 编译通过
- [ ] `mvn test` 所有测试通过
- [ ] 检查 git diff 确认没有遗漏的 import
- [ ] 使用 `mvn dependency:tree` 确认依赖版本正确

---

## 工具推荐

### 1. 检查 Java 版本兼容性
```bash
# 查看安装的 Java 版本
/usr/libexec/java_home -V

# 查看当前 Maven 使用的 Java 版本
mvn -version
```

### 2. 检查 Lombok 版本
```bash
# 查看 Maven 解析的 Lombok 版本
mvn help:effective-pom | grep lombok.version
```

### 3. 快速验证 API
```bash
# 创建临时测试类
cat > /tmp/TestApi.java << 'EOF'
import com.github.javaparser.*;
public class TestApi {
    public static void main(String[] args) {
        CompilationUnit cu = StaticJavaParser.parse("class Test {}");
        System.out.println(cu.getPrimaryTypeName());
    }
}
EOF
```

---

## 经验总结

### 根本原因

1. **假设而非验证** - 假设 JavaParser API 与记忆中的一致，未查阅文档验证
2. **缺少编译检查** - 编码过程中未定期执行 `mvn compile`
3. **IDE 警告被忽略** - 可能的 IDE 红色警告未及时修复
4. **依赖版本管理不当** - 未确认 pom.xml 中的依赖版本与实际使用的 API 匹配

### 改进措施

1. **文档先行** - 使用新依赖前，先阅读官方文档
2. **频繁编译** - 每完成一个类就执行一次编译
3. **零警告原则** - IDE 中不允许有编译警告
4. **依赖锁定** - 在 pom.xml 中明确指定所有依赖的版本号

---

## 参考资源

- [JavaParser API 文档](https://www.javadoc.io/doc/com.github.javaparser/javaparser-core/latest/index.html)
- [Lombok 功能列表](https://projectlombok.org/features/all)
- [Maven 编译器插件配置](https://maven.apache.org/plugins/maven-compiler-plugin/)
- [Java 关键字列表](https://docs.oracle.com/javase/tutorial/java/nutsandbolts/_keywords.html)

---

*创建时间：2026-03-31*  
*最后更新：2026-03-31*
