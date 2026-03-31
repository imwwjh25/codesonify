package com.codesonify;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * CodeSonify - 代码复杂度多维度可视化与声音化分析系统
 *
 * 通过多种感官维度（视觉 + 听觉）呈现代码复杂度
 * - 🎵 声音化：将代码复杂度映射为音乐
 * - 🎨 可视化：使用 Draw.io CLI 生成依赖关系图
 * - 📊 多维分析：圈复杂度、耦合度、代码行数、嵌套深度等
 *
 * @author CodeSonify Team
 * @since 1.0.0
 */
@SpringBootApplication
public class CodeSonifyApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeSonifyApplication.class, args);
    }
}
