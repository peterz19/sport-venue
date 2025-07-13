#!/bin/bash

echo "🧪 测试服务状态..."

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 30

# 测试配置中心
echo "测试配置中心..."
if curl -s http://localhost:8888/actuator/health | grep -q "UP"; then
    echo "✅ 配置中心运行正常"
else
    echo "❌ 配置中心未运行"
fi

# 测试注册中心
echo "测试注册中心..."
if curl -s http://localhost:8761/actuator/health | grep -q "UP"; then
    echo "✅ 注册中心运行正常"
else
    echo "❌ 注册中心未运行"
fi

# 测试用户服务
echo "测试用户服务..."
if curl -s http://localhost:8081/actuator/health | grep -q "UP"; then
    echo "✅ 用户服务运行正常"
else
    echo "❌ 用户服务未运行"
fi

# 测试场馆服务
echo "测试场馆服务..."
if curl -s http://localhost:8082/actuator/health | grep -q "UP"; then
    echo "✅ 场馆服务运行正常"
else
    echo "❌ 场馆服务未运行"
fi

# 测试预测服务
echo "测试预测服务..."
if curl -s http://localhost:8083/actuator/health | grep -q "UP"; then
    echo "✅ 预测服务运行正常"
else
    echo "❌ 预测服务未运行"
fi

# 测试社交服务
echo "测试社交服务..."
if curl -s http://localhost:8084/actuator/health | grep -q "UP"; then
    echo "✅ 社交服务运行正常"
else
    echo "❌ 社交服务未运行"
fi

# 测试网关服务
echo "测试网关服务..."
if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then
    echo "✅ 网关服务运行正常"
else
    echo "❌ 网关服务未运行"
fi

echo ""
echo "🎉 服务测试完成！"
echo "访问 http://localhost:8761 查看服务注册情况" 