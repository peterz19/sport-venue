#!/bin/bash

# 修复场馆数据库表结构
echo "开始修复场馆数据库表结构..."

# 检查MySQL是否运行
if ! pgrep -x "mysqld" > /dev/null; then
    echo "错误：MySQL服务未运行，请先启动MySQL服务"
    exit 1
fi

# 执行修复脚本
echo "执行数据库修复脚本..."
mysql -u root -pAa123456 < sport-venue-venue/src/main/resources/db/fix_venue_table.sql

if [ $? -eq 0 ]; then
    echo "数据库修复成功！"
    echo "现在可以重新启动场馆服务了。"
else
    echo "数据库修复失败，请检查MySQL连接和权限。"
    exit 1
fi 