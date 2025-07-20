#!/bin/bash

# 测试个人信息更新功能
echo "=== 测试个人信息更新功能 ==="

# 等待服务完全启动
sleep 5

# 测试1: 只更新基本信息（不包含userType）
echo "测试1: 只更新基本信息（不包含userType）"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "id": 1,
    "username": "admin",
    "realName": "测试管理员",
    "email": "test@example.com",
    "phone": "13800138000",
    "status": "active",
    "remark": "测试备注"
  }' | jq '.'

echo -e "\n"

# 测试2: 更新包含userType的信息
echo "测试2: 更新包含userType的信息"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "id": 1,
    "username": "admin",
    "realName": "测试管理员2",
    "email": "test2@example.com",
    "phone": "13800138001",
    "status": "active",
    "userType": "admin",
    "memberLevel": "gold",
    "remark": "测试备注2"
  }' | jq '.'

echo -e "\n"

# 测试3: 只更新部分字段
echo "测试3: 只更新部分字段"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "id": 1,
    "realName": "只更新姓名"
  }' | jq '.'

echo -e "\n"

# 测试4: 空字段测试
echo "测试4: 空字段测试"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "id": 1,
    "username": "admin"
  }' | jq '.'

echo -e "\n=== 测试完成 ===" 