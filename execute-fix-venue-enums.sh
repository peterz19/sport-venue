#!/bin/bash

# 修正场馆枚举值执行脚本
echo "=== 修正场馆枚举值SQL执行脚本 ==="
echo ""

# 检查是否有本地MySQL客户端
if command -v mysql &> /dev/null; then
    echo "检测到本地MySQL客户端，使用本地连接..."
    echo "执行SQL脚本..."
    mysql -u root -pAa123456 < fix-venue-enums.sql
elif command -v docker &> /dev/null; then
    echo "未检测到本地MySQL客户端，使用Docker MySQL..."
    echo "执行SQL脚本..."
    docker exec -i sport-venue-mysql-1 mysql -u root -proot@123 sport_venue < fix-venue-enums.sql
else
    echo "错误：未检测到MySQL客户端或Docker"
    echo "请手动执行 fix-venue-enums.sql 文件"
    echo "可以使用以下方式之一："
    echo "1. 安装MySQL客户端后运行：mysql -u root -pAa123456 < fix-venue-enums.sql"
    echo "2. 使用Docker：docker exec -i sport-venue-mysql-1 mysql -u root -proot@123 sport_venue < fix-venue-enums.sql"
    echo "3. 使用任何MySQL客户端工具（如Navicat、MySQL Workbench等）执行SQL文件"
    exit 1
fi

echo ""
echo "=== SQL执行完成 ==="
echo "请检查输出结果，确保所有枚举值都已正确修正" 