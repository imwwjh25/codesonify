package com.codesonify.domain.service;

import com.codesonify.domain.entity.DependencyGraph;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 依赖关系分析器
 *
 * 分析类之间的依赖关系，构建依赖图
 */
@Slf4j
@Service
public class DependencyAnalyzer {

    /**
     * 提取编译单元的依赖关系
     *
     * @param cu 编译单元
     * @return 依赖的类名列表
     */
    public List<String> extractDependencies(CompilationUnit cu) {
        Set<String> dependencies = new HashSet<>();

        // 提取 import 语句
        cu.getImports().forEach(importDecl -> {
            String importName = importDecl.getNameAsString();
            // 过滤掉 java.lang 和基础类型
            if (!importName.startsWith("java.lang") &&
                !importName.startsWith("java.util") &&
                !importName.startsWith("java.io")) {
                dependencies.add(importName);
            }
        });

        // 提取方法中使用的类型
        cu.accept(new com.github.javaparser.ast.visitor.VoidVisitorAdapter<Void>() {
            @Override
            public void visit(ClassOrInterfaceType n, Void arg) {
                String typeName = n.getNameAsString();
                // 只添加自定义类型（首字母大写）
                if (typeName.length() > 0 && Character.isUpperCase(typeName.charAt(0))) {
                    dependencies.add(typeName);
                }
                super.visit(n, arg);
            }
        }, null);

        return new ArrayList<>(dependencies);
    }

    /**
     * 计算类的耦合度 (CBO - Coupling Between Objects)
     *
     * @param clazz 类声明
     * @param cu 编译单元
     * @return CBO 值
     */
    public int calculateCBO(ClassOrInterfaceDeclaration clazz, CompilationUnit cu) {
        Set<String> dependencies = new HashSet<>();

        // 方法参数中的类型
        clazz.getMethods().forEach(method -> {
            method.getParameters().forEach(param -> {
                var type = param.getType();
                if (type.isClassOrInterfaceType()) {
                    dependencies.add(type.asClassOrInterfaceType().getNameAsString());
                }
            });

            // 方法返回类型
            var returnType = method.getType();
            if (returnType.isClassOrInterfaceType()) {
                dependencies.add(returnType.asClassOrInterfaceType().getNameAsString());
            }
        });

        // 字段类型
        clazz.getFields().forEach(field -> {
            field.getVariables().forEach(var -> {
                var type = var.getType();
                if (type.isClassOrInterfaceType()) {
                    dependencies.add(type.asClassOrInterfaceType().getNameAsString());
                }
            });
        });

        // 父类和实现的接口
        clazz.getExtendedTypes().forEach(type -> {
            dependencies.add(type.getNameAsString());
        });
        clazz.getImplementedTypes().forEach(type -> {
            dependencies.add(type.getNameAsString());
        });

        // 移除自身类和 java.lang 类型
        dependencies.remove(clazz.getNameAsString());
        dependencies.removeIf(d -> d.startsWith("java.lang"));

        return dependencies.size();
    }

    /**
     * 分析多个编译单元的依赖关系，构建依赖图
     *
     * @param compilationUnits 编译单元列表
     * @return 依赖图
     */
    public DependencyGraph analyzeDependencies(List<CompilationUnit> compilationUnits) {
        log.info("分析 {} 个编译单元的依赖关系", compilationUnits.size());

        DependencyGraph graph = new DependencyGraph();

        // 收集所有类名
        Map<String, Set<String>> classDependencies = new HashMap<>();

        for (CompilationUnit cu : compilationUnits) {
            String filePath = cu.getStorage().map(s -> s.getPath().toString()).orElse("");
            String className = cu.getPrimaryTypeName().orElse(null);

            if (className == null) continue;

            // 添加节点
            graph.addNode(className);

            // 提取依赖
            List<String> dependencies = extractDependencies(cu);
            classDependencies.put(className, new HashSet<>(dependencies));
        }

        // 添加边（只添加到已知的类节点）
        for (Map.Entry<String, Set<String>> entry : classDependencies.entrySet()) {
            String sourceClass = entry.getKey();
            for (String dependency : entry.getValue()) {
                if (graph.getNodes().contains(dependency)) {
                    graph.addEdge(sourceClass, dependency);
                }
            }
        }

        // 计算图的指标
        graph.calculateMetrics();

        log.info("依赖图构建完成：{} 个节点，{} 条边，{} 个循环",
                graph.getStatistics().getNodeCount(),
                graph.getStatistics().getEdgeCount(),
                graph.getStatistics().getCycleCount());

        return graph;
    }

    /**
     * 获取类的依赖列表
     */
    public Map<String, List<String>> getClassDependencies(List<CompilationUnit> compilationUnits) {
        Map<String, List<String>> result = new HashMap<>();

        for (CompilationUnit cu : compilationUnits) {
            cu.getPrimaryTypeName().ifPresent(className -> {
                result.put(className, extractDependencies(cu));
            });
        }

        return result;
    }
}
