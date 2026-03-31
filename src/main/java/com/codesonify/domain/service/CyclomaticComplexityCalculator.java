package com.codesonify.domain.service;

import com.codesonify.domain.entity.ComplexityLevel;
import com.codesonify.domain.entity.ComplexityMetrics;
import com.codesonify.domain.entity.MethodType;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 圈复杂度计算器
 *
 * 基于 JavaParser AST 遍历，统计决策点数量
 * 公式：CC = 1 + 决策点数量
 */
@Service
public class CyclomaticComplexityCalculator {

    /**
     * 计算方法的圈复杂度
     *
     * @param method 方法声明
     * @return 圈复杂度值
     */
    public int calculate(MethodDeclaration method) {
        int[] complexity = {1}; // 基础复杂度

        // 遍历方法体，统计决策点
        method.accept(new VoidVisitorAdapter<Void>() {

            @Override
            public void visit(IfStmt n, Void arg) {
                complexity[0]++; // if 语句
                super.visit(n, arg);
            }

            @Override
            public void visit(ForStmt n, Void arg) {
                complexity[0]++; // for 循环
                super.visit(n, arg);
            }

            @Override
            public void visit(ForEachStmt n, Void arg) {
                complexity[0]++; // foreach 循环
                super.visit(n, arg);
            }

            @Override
            public void visit(WhileStmt n, Void arg) {
                complexity[0]++; // while 循环
                super.visit(n, arg);
            }

            @Override
            public void visit(DoStmt n, Void arg) {
                complexity[0]++; // do-while 循环
                super.visit(n, arg);
            }

            @Override
            public void visit(SwitchEntry n, Void arg) {
                if (!n.getLabels().isEmpty()) {
                    complexity[0]++; // switch case
                }
                super.visit(n, arg);
            }

            @Override
            public void visit(CatchClause n, Void arg) {
                complexity[0]++; // catch 块
                super.visit(n, arg);
            }

            @Override
            public void visit(ConditionalExpr n, Void arg) {
                complexity[0]++; // 三元运算符
                super.visit(n, arg);
            }

            @Override
            public void visit(BinaryExpr n, Void arg) {
                // 逻辑运算符 && 和 ||
                if (n.getOperator() == BinaryExpr.Operator.AND ||
                    n.getOperator() == BinaryExpr.Operator.OR) {
                    complexity[0]++;
                }
                super.visit(n, arg);
            }
        }, null);

        return complexity[0];
    }

    /**
     * 计算嵌套深度
     *
     * @param method 方法声明
     * @return 嵌套深度
     */
    public int calculateNestingDepth(MethodDeclaration method) {
        int[] maxDepth = {0};
        calculateNestingDepthRecursive(method.getBody().orElse(null), 0, maxDepth);
        return maxDepth[0];
    }

    private void calculateNestingDepthRecursive(Statement stmt, int currentDepth, int[] maxDepth) {
        if (stmt == null) return;

        if (stmt instanceof IfStmt) {
            int newDepth = currentDepth + 1;
            maxDepth[0] = Math.max(maxDepth[0], newDepth);
            IfStmt ifStmt = (IfStmt) stmt;
            calculateNestingDepthRecursive(ifStmt.getThenStmt(), newDepth, maxDepth);
            ifStmt.getElseStmt().ifPresent(elseStmt ->
                calculateNestingDepthRecursive(elseStmt, newDepth, maxDepth));
        } else if (stmt instanceof ForStmt || stmt instanceof ForeachStmt || stmt instanceof WhileStmt) {
            int newDepth = currentDepth + 1;
            maxDepth[0] = Math.max(maxDepth[0], newDepth);
            // 继续遍历循环体
        } else if (stmt instanceof BlockStmt) {
            BlockStmt block = (BlockStmt) stmt;
            for (Statement inner : block.getStatements()) {
                calculateNestingDepthRecursive(inner, currentDepth, maxDepth);
            }
        }
    }
}
