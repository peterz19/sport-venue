#!/bin/bash

echo "🛑 停止Sport Venue微服务集群..."

# 停止所有Java进程
if [ -d "logs" ]; then
    for pid_file in logs/*.pid; do
        if [ -f "$pid_file" ]; then
            pid=$(cat "$pid_file")
            if kill -0 "$pid" 2>/dev/null; then
                echo "停止进程 $pid..."
                kill "$pid"
            fi
        fi
    done
fi

# 停止Docker容器
echo "停止Docker容器..."
docker-compose down

echo "✅ 所有服务已停止！" 