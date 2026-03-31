package com.codesonify.domain.service;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 圈复杂度计算器单元测试
 */
class CyclomaticComplexityCalculatorTest {

    private CyclomaticComplexityCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new CyclomaticComplexityCalculator();
    }

    @Test
    @DisplayName("简单方法 - 圈复杂度 CC=1")
    void testSimpleMethod_CC1() throws Exception {
        String code = """
            public class Test {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(1, complexity, "简单方法的圈复杂度应为 1");
    }

    @Test
    @DisplayName("单个 if 语句 - 圈复杂度 CC=2")
    void testIfStatement_CC2() throws Exception {
        String code = """
            public class Test {
                public int max(int a, int b) {
                    if (a > b) {
                        return a;
                    }
                    return b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(2, complexity, "单个 if 语句的圈复杂度应为 2");
    }

    @Test
    @DisplayName("if-else 语句 - 圈复杂度 CC=2")
    void testIfElseStatement_CC2() throws Exception {
        String code = """
            public class Test {
                public int abs(int a) {
                    if (a >= 0) {
                        return a;
                    } else {
                        return -a;
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(2, complexity, "if-else 语句的圈复杂度应为 2");
    }

    @Test
    @DisplayName("for 循环 - 圈复杂度 CC=2")
    void testForLoop_CC2() throws Exception {
        String code = """
            public class Test {
                public int sum(int[] arr) {
                    int sum = 0;
                    for (int i = 0; i < arr.length; i++) {
                        sum += arr[i];
                    }
                    return sum;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(2, complexity, "for 循环的圈复杂度应为 2");
    }

    @Test
    @DisplayName("嵌套 if 语句 - 圈复杂度 CC=3")
    void testNestedIf_CC3() throws Exception {
        String code = """
            public class Test {
                public int max(int a, int b, int c) {
                    if (a > b) {
                        if (a > c) {
                            return a;
                        }
                        return c;
                    }
                    return b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(3, complexity, "嵌套 if 语句的圈复杂度应为 3");
    }

    @Test
    @DisplayName("逻辑运算符 && - 圈复杂度 CC=3")
    void testLogicalAnd_CC3() throws Exception {
        String code = """
            public class Test {
                public void check(int a, int b) {
                    if (a > 0 && b > 0) {
                        System.out.println("both positive");
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(3, complexity, "if + && 的圈复杂度应为 3");
    }

    @Test
    @DisplayName("逻辑运算符 || - 圈复杂度 CC=3")
    void testLogicalOr_CC3() throws Exception {
        String code = """
            public class Test {
                public void check(int a, int b) {
                    if (a > 0 || b > 0) {
                        System.out.println("at least one positive");
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(3, complexity, "if + || 的圈复杂度应为 3");
    }

    @Test
    @DisplayName("switch 语句 - 圈复杂度 CC=4")
    void testSwitch_CC4() throws Exception {
        String code = """
            public class Test {
                public String dayName(int day) {
                    switch (day) {
                        case 1: return "Monday";
                        case 2: return "Tuesday";
                        case 3: return "Wednesday";
                        default: return "Unknown";
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertTrue(complexity >= 3, "switch 语句应该有更高的圈复杂度");
    }

    @Test
    @DisplayName("三元运算符 - 圈复杂度 CC=2")
    void testTernaryOperator_CC2() throws Exception {
        String code = """
            public class Test {
                public int max(int a, int b) {
                    return a > b ? a : b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertEquals(2, complexity, "三元运算符的圈复杂度应为 2");
    }

    @Test
    @DisplayName("复杂方法 - 圈复杂度 CC=5+")
    void testComplexMethod() throws Exception {
        String code = """
            public class Test {
                public void process(List<String> items) {
                    for (String item : items) {
                        if (item != null && item.length() > 0) {
                            if (item.startsWith("A") || item.startsWith("B")) {
                                System.out.println("Found: " + item);
                            }
                        }
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int complexity = calculator.calculate(method);

        assertTrue(complexity >= 5, "复杂方法应该有较高的圈复杂度，实际：" + complexity);
    }

    @Test
    @DisplayName("计算嵌套深度 - 简单方法")
    void testNestingDepth_Simple() throws Exception {
        String code = """
            public class Test {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int depth = calculator.calculateNestingDepth(method);

        assertEquals(0, depth, "简单方法的嵌套深度应为 0");
    }

    @Test
    @DisplayName("计算嵌套深度 - 嵌套 if)")
    void testNestingDepth_Nested() throws Exception {
        String code = """
            public class Test {
                public void check(int a, int b, int c) {
                    if (a > 0) {
                        if (b > 0) {
                            if (c > 0) {
                                System.out.println("all positive");
                            }
                        }
                    }
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int depth = calculator.calculateNestingDepth(method);

        assertEquals(3, depth, "三层 if 嵌套的深度应为 3");
    }

    @Test
    @DisplayName("计算代码行数")
    void testLinesOfCode() throws Exception {
        String code = """
            public class Test {
                public int add(int a, int b) {
                    return a + b;
                }
            }
            """;

        CompilationUnit cu = StaticJavaParser.parse(code);
        MethodDeclaration method = cu.findFirst(MethodDeclaration.class).orElseThrow();

        int lines = calculator.calculateLinesOfCode(method);

        assertTrue(lines >= 2, "方法应该有至少 2 行代码");
    }
}
