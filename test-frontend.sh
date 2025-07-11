#!/bin/bash

echo "🧪 测试前端连通性..."

# 测试B端管理后台
echo ""
echo "📱 测试B端管理后台 (http://localhost:3000)..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:3000 | grep -q "200"; then
    echo "✅ B端管理后台连接正常"
else
    echo "❌ B端管理后台连接失败"
    echo "💡 请确保B端管理后台已启动: cd sport-venue-admin && npm run dev"
fi

# 测试商户端前端
echo ""
echo "🏪 测试商户端前端 (http://localhost:3001)..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:3001 | grep -q "200"; then
    echo "✅ 商户端前端连接正常"
else
    echo "❌ 商户端前端连接失败"
    echo "💡 请确保商户端前端已启动: cd sport-venue-merchant && npm run dev"
fi

# 测试后端API
echo ""
echo "🔗 测试后端API (http://localhost:8082)..."
if curl -s -o /dev/null -w "%{http_code}" http://localhost:8082/venues | grep -q "200\|401\|403"; then
    echo "✅ 后端API连接正常"
else
    echo "❌ 后端API连接失败"
    echo "💡 请确保后端服务已启动: ./start-services.sh"
fi

echo ""
echo "🎯 测试完成！"
echo ""
echo "📋 访问地址:"
echo "   - B端管理后台: http://localhost:3000"
echo "   - 商户端前端: http://localhost:3001"
echo "   - 后端API文档: http://localhost:8082/swagger-ui.html"
echo ""
echo "🔧 如果连接失败，请检查:"
echo "   1. 后端服务是否启动: ./start-services.sh"
echo "   2. 前端服务是否启动: ./start-frontend.sh"
echo "   3. 端口是否被占用: lsof -i :3000, lsof -i :3001, lsof -i :8082" 