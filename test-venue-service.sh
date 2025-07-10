#!/bin/bash

# 设置JDK 21环境
export JAVA_HOME=/opt/homebrew/Cellar/openjdk@21/21.0.7/libexec/openjdk.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

echo "=== 场馆服务启动测试 ==="
echo "使用JDK版本:"
java -version

echo ""
echo "检查Redis连接..."
if redis-cli ping > /dev/null 2>&1; then
    echo "✓ Redis连接正常"
else
    echo "✗ Redis连接失败"
    exit 1
fi

echo ""
echo "编译项目..."
cd sport-venue-venue
if mvn clean compile -q; then
    echo "✓ 编译成功"
else
    echo "✗ 编译失败"
    exit 1
fi

echo ""
echo "启动场馆服务（后台运行）..."
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../venue-service.log 2>&1 &
SERVICE_PID=$!

echo "服务PID: $SERVICE_PID"
echo "等待服务启动..."

# 等待30秒
for i in {1..30}; do
    if curl -s http://localhost:8082/actuator/health > /dev/null 2>&1; then
        echo "✓ 场馆服务启动成功！"
        echo "健康检查:"
        curl -s http://localhost:8082/health | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8082/health
        echo ""
        echo "API测试:"
        echo "获取场馆列表:"
        curl -s http://localhost:8082/venues | python3 -m json.tool 2>/dev/null || curl -s http://localhost:8082/venues
        echo ""
        echo "服务日志位置: ../venue-service.log"
        echo "停止服务命令: kill $SERVICE_PID"
        exit 0
    fi
    echo "等待中... ($i/30)"
    sleep 1
done

echo "✗ 服务启动超时"
echo "查看日志:"
tail -20 ../venue-service.log
kill $SERVICE_PID 2>/dev/null
exit 1 