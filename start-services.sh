#!/bin/bash

echo "🚀 启动Sport Venue微服务集群..."

# 检查Docker是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker未运行，请先启动Docker"
    exit 1
fi

# 启动基础设施
echo "📦 启动基础设施服务..."
docker-compose up -d

# 等待基础设施启动
echo "⏳ 等待基础设施启动..."
sleep 10

# 启动配置中心
echo "⚙️  启动配置中心..."
cd sport-venue-config
mvn spring-boot:run > ../logs/config.log 2>&1 &
CONFIG_PID=$!
cd ..

# 等待配置中心启动
echo "⏳ 等待配置中心启动..."
sleep 15

# 启动注册中心
echo "🔍 启动注册中心..."
cd sport-venue-eureka
mvn spring-boot:run > ../logs/eureka.log 2>&1 &
EUREKA_PID=$!
cd ..

# 等待注册中心启动
echo "⏳ 等待注册中心启动..."
sleep 15

# 启动业务服务
echo "🏃 启动业务服务..."

# 用户服务
echo "👤 启动用户服务..."
cd sport-venue-user
mvn spring-boot:run > ../logs/user.log 2>&1 &
USER_PID=$!
cd ..

# 场馆服务
echo "🏟️  启动场馆服务..."
cd sport-venue-venue
mvn spring-boot:run > ../logs/venue.log 2>&1 &
VENUE_PID=$!
cd ..

# 预测服务
echo "🔮 启动预测服务..."
cd sport-venue-prediction
mvn spring-boot:run > ../logs/prediction.log 2>&1 &
PREDICTION_PID=$!
cd ..

# 社交服务
echo "💬 启动社交服务..."
cd sport-venue-social
mvn spring-boot:run > ../logs/social.log 2>&1 &
SOCIAL_PID=$!
cd ..

# 网关服务
echo "🚪 启动网关服务..."
cd sport-venue-gateway
mvn spring-boot:run > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

# 创建日志目录
mkdir -p logs

# 保存PID到文件
echo $CONFIG_PID > logs/config.pid
echo $EUREKA_PID > logs/eureka.pid
echo $USER_PID > logs/user.pid
echo $VENUE_PID > logs/venue.pid
echo $PREDICTION_PID > logs/prediction.pid
echo $SOCIAL_PID > logs/social.pid
echo $GATEWAY_PID > logs/gateway.pid

echo "✅ 所有服务启动完成！"
echo ""
echo "📊 服务状态："
echo "  - 配置中心: http://localhost:8888"
echo "  - 注册中心: http://localhost:8761"
echo "  - 网关服务: http://localhost:8080"
echo "  - 用户服务: http://localhost:8081"
echo "  - 场馆服务: http://localhost:8082"
echo "  - 预测服务: http://localhost:8083"
echo "  - 社交服务: http://localhost:8084"
echo ""
echo "📝 日志文件保存在 logs/ 目录下"
echo "🛑 使用 ./stop-services.sh 停止所有服务" 