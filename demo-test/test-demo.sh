#!/bin/bash

# CodeSonify MVP 测试脚本
# 用于演示代码复杂度分析、声音化和报告生成功能

set -e

echo "========================================"
echo "  CodeSonify MVP 功能演示测试"
echo "========================================"
echo ""

# 配置
BASE_URL="http://localhost:8080"
DEMO_PROJECT_PATH="$(cd "$(dirname "$0")/sample-project" && pwd)"

echo "📁 测试项目路径：$DEMO_PROJECT_PATH"
echo "🌐 API 地址：$BASE_URL"
echo ""

# 检查服务是否运行
echo "⏳ 检查 CodeSonify 服务状态..."
if curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/swagger-ui.html" | grep -q "200\|302"; then
    echo "✅ 服务运行正常"
else
    echo "❌ 服务未运行，请先启动应用："
    echo "   mvn spring-boot:run"
    exit 1
fi
echo ""

# 1. 代码分析
echo "========================================"
echo "  步骤 1: 代码复杂度分析"
echo "========================================"
echo ""

ANALYZE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/analysis/analyze?projectPath=$DEMO_PROJECT_PATH" \
    -H "Content-Type: application/json")

echo "📊 分析响应:"
echo "$ANALYZE_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ANALYZE_RESPONSE"
echo ""

# 提取统计信息（简化处理）
TOTAL_CLASSES=$(echo "$ANALYZE_RESPONSE" | grep -o '"totalClasses":[0-9]*' | cut -d: -f2)
AVG_COMPLEXITY=$(echo "$ANALYZE_RESPONSE" | grep -o '"averageCyclomaticComplexity":[0-9.]*' | cut -d: -f2)

echo "📈 分析结果摘要:"
echo "   - 总类数：${TOTAL_CLASSES:-N/A}"
echo "   - 平均复杂度：${AVG_COMPLEXITY:-N/A}"
echo ""

# 2. 生成 HTML 报告
echo "========================================"
echo "  步骤 2: 生成 HTML 报告"
echo "========================================"
echo ""

# 由于 MVP 版本没有缓存，我们直接生成报告到文件
echo "📄 访问前端页面查看报告:"
echo "   $BASE_URL/"
echo ""
echo "📚 查看 API 文档:"
echo "   $BASE_URL/swagger-ui.html"
echo ""

# 3. 生成 MIDI 音乐（需要分析 ID，这里演示 API 调用）
echo "========================================"
echo "  步骤 3: 代码声音化（演示）"
echo "========================================"
echo ""
echo "🎵 声音化功能需要分析结果缓存后才能使用"
echo "   在 Redis 配置后可用以下 API:"
echo "   POST $BASE_URL/api/sonification/generate?analysisId=<id>"
echo ""

# 4. 前端页面测试
echo "========================================"
echo "  步骤 4: 前端页面测试"
echo "========================================"
echo ""

HOME_PAGE=$(curl -s "$BASE_URL/")
if echo "$HOME_PAGE" | grep -q "CodeSonify"; then
    echo "✅ 前端页面可访问"
    echo "   访问地址：$BASE_URL/"
else
    echo "⚠️  前端页面可能有问题"
fi
echo ""

# 5. 测试总结
echo "========================================"
echo "  测试总结"
echo "========================================"
echo ""
echo "✅ 已完成的测试:"
echo "   1. 代码复杂度分析 API"
echo "   2. 服务健康检查"
echo "   3. 前端页面访问"
echo ""
echo "📝 手动测试建议:"
echo "   1. 打开浏览器访问：$BASE_URL/"
echo "   2. 在页面中输入项目路径：$DEMO_PROJECT_PATH"
echo "   3. 点击'同步分析'按钮"
echo "   4. 查看分析结果和统计信息"
echo "   5. 访问 Swagger 文档：$BASE_URL/swagger-ui.html"
echo ""
echo "========================================"
echo "  测试完成!"
echo "========================================"
