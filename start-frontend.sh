#!/bin/bash

echo "🚀 启动运动场馆管理系统前端..."

# 检查Node.js是否安装
if ! command -v node &> /dev/null; then
    echo "❌ Node.js未安装，请先安装Node.js"
    echo "💡 可以运行: ./install-nodejs.sh 来安装Node.js"
    exit 1
fi

# 检查npm是否安装
if ! command -v npm &> /dev/null; then
    echo "❌ npm未安装，请先安装npm"
    exit 1
fi

echo "✅ Node.js版本: $(node --version)"
echo "✅ npm版本: $(npm --version)"

# 启动B端管理后台
echo ""
echo "📱 启动B端管理后台 (端口: 3000)..."
cd sport-venue-admin

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装B端管理后台依赖..."
    npm install
fi

# 启动B端管理后台
echo "🚀 启动B端管理后台..."
npm run dev &
ADMIN_PID=$!

# 等待B端管理后台启动
sleep 5

# 启动商户端前端
echo ""
echo "🏪 启动商户端前端 (端口: 3001)..."
cd ../sport-venue-merchant

# 检查是否已安装依赖
if [ ! -d "node_modules" ]; then
    echo "📦 安装商户端前端依赖..."
    npm install
fi

# 启动商户端前端
echo "🚀 启动商户端前端..."
npm run dev &
MERCHANT_PID=$!

# 等待商户端前端启动
sleep 5

echo ""
echo "🎉 前端服务启动完成！"
echo ""
echo "📱 B端管理后台: http://localhost:3000"
echo "🏪 商户端前端: http://localhost:3001"
echo ""
echo "💡 使用说明:"
echo "   - B端管理后台: 管理员使用，可以管理所有场馆"
echo "   - 商户端前端: 商户使用，只能管理自己的场馆"
echo ""
echo "🔧 商户端测试账号:"
echo "   - 商户ID: merchant001"
echo "   - 密码: 123456"
echo ""
echo "⏹️  停止服务: 按 Ctrl+C 或运行 ./stop-frontend.sh"

# 等待用户中断
wait 