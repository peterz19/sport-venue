#!/bin/bash

echo "🛑 停止前端服务..."

# 查找并停止Node.js进程
echo "🔍 查找前端服务进程..."

# 停止端口3000的进程
PIDS_3000=$(lsof -ti:3000)
if [ ! -z "$PIDS_3000" ]; then
    echo "🛑 停止端口3000的进程: $PIDS_3000"
    kill -9 $PIDS_3000
fi

# 停止端口3001的进程
PIDS_3001=$(lsof -ti:3001)
if [ ! -z "$PIDS_3001" ]; then
    echo "🛑 停止端口3001的进程: $PIDS_3001"
    kill -9 $PIDS_3001
fi

# 查找并停止npm进程
NPM_PIDS=$(pgrep -f "npm run dev")
if [ ! -z "$NPM_PIDS" ]; then
    echo "🛑 停止npm进程: $NPM_PIDS"
    kill -9 $NPM_PIDS
fi

# 查找并停止vite进程
VITE_PIDS=$(pgrep -f "vite")
if [ ! -z "$VITE_PIDS" ]; then
    echo "🛑 停止vite进程: $VITE_PIDS"
    kill -9 $VITE_PIDS
fi

echo "✅ 前端服务已停止" 