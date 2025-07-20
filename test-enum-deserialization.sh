#!/bin/bash

# 测试枚举反序列化功能
echo "=== 测试枚举反序列化功能 ==="

# 等待服务完全启动
sleep 5

# 测试1: 使用小写的status值
echo "测试1: 使用小写的status值 (active)"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "username": "testuser",
    "realName": "测试用户",
    "email": "test@example.com",
    "phone": "13800138000",
    "status": "active",
    "userType": "admin",
    "memberLevel": "gold"
  }' | jq '.'

echo -e "\n"

# 测试2: 使用中文的status值
echo "测试2: 使用中文的status值 (正常)"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "username": "testuser2",
    "realName": "测试用户2",
    "email": "test2@example.com",
    "phone": "13800138001",
    "status": "正常",
    "userType": "系统管理员",
    "memberLevel": "黄金"
  }' | jq '.'

echo -e "\n"

# 测试3: 使用混合大小写的值
echo "测试3: 使用混合大小写的值 (Active)"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "username": "testuser3",
    "realName": "测试用户3",
    "email": "test3@example.com",
    "phone": "13800138002",
    "status": "Active",
    "userType": "Admin",
    "memberLevel": "Gold"
  }' | jq '.'

echo -e "\n"

# 测试4: 使用无效值（应该返回默认值）
echo "测试4: 使用无效值（应该返回默认值）"
curl -X PUT "http://localhost:8082/api/users/profile" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer test-token" \
  -d '{
    "username": "testuser4",
    "realName": "测试用户4",
    "email": "test4@example.com",
    "phone": "13800138003",
    "status": "invalid_status",
    "userType": "invalid_type",
    "memberLevel": "invalid_level"
  }' | jq '.'

echo -e "\n=== 测试完成 ===" 