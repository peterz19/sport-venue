#!/bin/bash

echo "🔍 检查本地环境..."

# 检查Java版本
echo "检查Java版本..."
if command -v java &> /dev/null; then
    java_version=$(java -version 2>&1 | head -n 1 | cut -d'"' -f2)
    echo "✅ Java版本: $java_version"
else
    echo "❌ Java未安装"
    exit 1
fi

# 检查Maven版本
echo "检查Maven版本..."
if command -v mvn &> /dev/null; then
    mvn_version=$(mvn -version | head -n 1 | cut -d' ' -f3)
    echo "✅ Maven版本: $mvn_version"
else
    echo "❌ Maven未安装"
    exit 1
fi

# 检查MySQL
echo "检查MySQL连接..."

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

if $MYSQL_CMD -h localhost -P 3306 -u root -p'Aa123456' -e "SELECT 1;" > /dev/null 2>&1; then
    echo "✅ MySQL连接正常"
    
    # 检查数据库是否存在
    if $MYSQL_CMD -h localhost -P 3306 -u root -p'Aa123456' -e "USE sport_venue;" > /dev/null 2>&1; then
        echo "✅ 数据库sport_venue存在"
    else
        echo "⚠️  数据库sport_venue不存在，正在创建..."
        $MYSQL_CMD -h localhost -P 3306 -u root -p'Aa123456' -e "CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
        echo "✅ 数据库sport_venue创建完成"
    fi
else
    echo "❌ MySQL连接失败"
    echo "请确保MySQL在localhost:3306运行，用户名: root，密码: Aa123456"
    echo "或者检查MySQL服务状态：brew services list | grep mysql"
    exit 1
fi

# 检查Redis
echo "检查Redis连接..."
if redis-cli -h localhost -p 6379 ping > /dev/null 2>&1; then
    echo "✅ Redis连接正常"
else
    echo "❌ Redis连接失败"
    echo "请确保Redis在localhost:6379运行，无密码"
    exit 1
fi

# 检查端口占用
echo "检查端口占用情况..."
ports=(8888 8761 8080 8081 8082 8083 8084)
for port in "${ports[@]}"; do
    if lsof -i :$port > /dev/null 2>&1; then
        echo "⚠️  端口 $port 被占用"
    else
        echo "✅ 端口 $port 可用"
    fi
done

echo ""
echo "🎉 环境检查完成！"
echo "如果所有检查都通过，可以运行 ./start-local-services.sh 启动服务" 