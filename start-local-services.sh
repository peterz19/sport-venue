#!/bin/bash

echo "🚀 启动Sport Venue微服务集群（本地模式）..."

# 检查必要的本地服务
echo "🔍 检查本地服务状态..."
# 尝试找到mysql命令
MYSQL_CMD=""
if command -v mysql &> /dev/null; then
    MYSQL_CMD="mysql"
elif [ -f "/usr/local/mysql/bin/mysql" ]; then
    MYSQL_CMD="/usr/local/mysql/bin/mysql"
elif [ -f "/opt/homebrew/bin/mysql" ]; then
    MYSQL_CMD="/opt/homebrew/bin/mysql"
elif [ -f "/usr/local/bin/mysql" ]; then
    MYSQL_CMD="/usr/local/bin/mysql"
else
    echo "❌ 未找到mysql命令"
    echo "请确保MySQL已正确安装，或者手动创建数据库："
    echo "mysql -u root -pAa123456 -e \"CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""
    exit 1
fi

# 检查MySQL
if ! $MYSQL_CMD -h localhost -P 3306 -u root -p'Aa123456' -e "SELECT 1;" > /dev/null 2>&1; then
    echo "❌ MySQL未运行或连接失败"
    echo "请确保MySQL在localhost:3306运行，用户名: root，密码: Aa123456"
    echo "或者修改配置文件中的数据库连接信息"
    exit 1
fi

# 检查Redis
if ! redis-cli -h localhost -p 6379  ping > /dev/null 2>&1; then
    echo "❌ Redis未运行或连接失败"
    echo "请确保Redis在localhost:6379运行，密码:  "
    echo "或者修改配置文件中的Redis连接信息"
    exit 1
fi

echo "✅ 本地服务检查通过"

# 创建日志目录
mkdir -p logs

# 启动配置中心
echo "⚙️  启动配置中心..."
cd sport-venue-config
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/config.log 2>&1 &
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
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/user.log 2>&1 &
USER_PID=$!
cd ..

# 场馆服务
echo "🏟️  启动场馆服务..."
cd sport-venue-venue
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/venue.log 2>&1 &
VENUE_PID=$!
cd ..

# 预测服务
echo "🔮 启动预测服务..."
cd sport-venue-prediction
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/prediction.log 2>&1 &
PREDICTION_PID=$!
cd ..

# 社交服务
echo "💬 启动社交服务..."
cd sport-venue-social
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/social.log 2>&1 &
SOCIAL_PID=$!
cd ..

# 网关服务
echo "🚪 启动网关服务..."
cd sport-venue-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=local > ../logs/gateway.log 2>&1 &
GATEWAY_PID=$!
cd ..

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