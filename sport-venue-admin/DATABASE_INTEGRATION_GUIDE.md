# 个人信息页面数据库集成指南

## 概述

个人信息页面现在已经与数据库完全集成，用户信息会持久化存储在数据库中，不再依赖本地缓存。

## 数据库表结构

### users表
```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    real_name VARCHAR(50),
    phone VARCHAR(20) UNIQUE,
    email VARCHAR(100) UNIQUE,
    user_type VARCHAR(20) NOT NULL,
    merchant_id BIGINT,
    merchant_name VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    avatar VARCHAR(200),
    points INT DEFAULT 0,
    member_level VARCHAR(20) DEFAULT 'BRONZE',
    last_login_time DATETIME,
    last_login_ip VARCHAR(50),
    remark VARCHAR(500),
    create_time DATETIME NOT NULL,
    update_time DATETIME NOT NULL,
    create_by BIGINT,
    update_by BIGINT
);
```

## API接口

### 1. 获取用户个人信息
- **URL**: `GET /users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **响应示例**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "admin",
    "realName": "管理员",
    "email": "admin@example.com",
    "phone": "13800138000",
    "avatar": "http://example.com/avatar.jpg",
    "userType": "ADMIN",
    "status": "ACTIVE",
    "createTime": "2024-01-01T00:00:00",
    "lastLoginTime": "2024-01-01T12:00:00"
  }
}
```

### 2. 更新用户个人信息
- **URL**: `PUT /users/profile`
- **Headers**: `Authorization: Bearer {token}`
- **请求体示例**:
```json
{
  "id": 1,
  "username": "admin",
  "realName": "新姓名",
  "email": "newemail@example.com",
  "phone": "13900139000",
  "avatar": "http://example.com/new-avatar.jpg",
  "remark": "个人简介"
}
```

## 前端集成

### 数据加载流程
1. 页面加载时首先从本地存储获取默认值
2. 调用后端API `/users/profile` 获取最新用户信息
3. 将后端数据转换为前端格式并更新表单
4. 同时更新本地存储作为缓存

### 数据保存流程
1. 用户点击保存按钮
2. 验证表单数据
3. 将前端数据转换为后端格式
4. 调用后端API `/users/profile` 更新用户信息
5. 保存成功后重新加载用户信息确保数据同步
6. 更新本地存储

### 数据格式转换

#### 前端到后端
```javascript
const userUpdateData = {
  id: form.id,
  username: form.username,
  realName: saveData.realName,
  email: saveData.email,
  phone: saveData.phone,
  avatar: form.avatar,
  remark: saveData.bio,
  userType: form.userType,
  status: form.status,
  points: form.points,
  memberLevel: form.memberLevel
}
```

#### 后端到前端
```javascript
const userData = {
  id: response.id,
  username: response.username,
  realName: response.realName || '',
  email: response.email || '',
  phone: response.phone || '',
  gender: response.gender || 'male',
  birthday: response.birthday || '',
  bio: response.remark || '',
  address: response.address || '',
  avatar: response.avatar || '',
  createTime: response.createTime,
  lastLoginTime: response.lastLoginTime,
  status: response.status?.toLowerCase() || 'active',
  role: response.userType?.description || '管理员'
}
```

## 测试步骤

### 1. 启动后端服务
```bash
# 启动场馆服务
cd sport-venue-venue
mvn spring-boot:run
```

### 2. 启动前端服务
```bash
cd sport-venue-admin
npm run dev
```

### 3. 测试流程
1. 登录系统
2. 访问个人信息页面
3. 编辑个人信息
4. 保存更改
5. 退出登录
6. 重新登录
7. 验证信息是否保持

### 4. 数据库验证
```sql
-- 查看用户表数据
SELECT id, username, real_name, email, phone, avatar, update_time 
FROM users 
WHERE username = 'admin';

-- 查看用户更新历史
SELECT * FROM users WHERE username = 'admin' ORDER BY update_time DESC;
```

## 错误处理

### 常见错误及解决方案

#### 1. 404错误
- **原因**: API接口不存在
- **解决**: 确保后端服务正常运行，API路径正确

#### 2. 401错误
- **原因**: Token无效或过期
- **解决**: 重新登录获取新token

#### 3. 403错误
- **原因**: 权限不足
- **解决**: 检查用户权限设置

#### 4. 500错误
- **原因**: 服务器内部错误
- **解决**: 检查后端日志，数据库连接等

## 数据同步策略

### 1. 实时同步
- 用户操作后立即调用API更新数据库
- 保存成功后重新加载数据确保一致性

### 2. 本地缓存
- 使用localStorage作为本地缓存
- 提高页面加载速度
- 离线时仍可查看基本信息

### 3. 冲突处理
- 优先使用后端数据
- 本地缓存作为备用方案
- 数据不一致时自动同步

## 安全考虑

### 1. 身份验证
- 所有API调用都需要有效的JWT token
- Token过期自动跳转登录页

### 2. 权限控制
- 用户只能更新自己的个人信息
- 验证用户ID与token中的用户ID一致

### 3. 数据验证
- 前端表单验证
- 后端数据验证
- SQL注入防护

## 性能优化

### 1. 缓存策略
- 本地存储缓存用户基本信息
- 减少不必要的API调用

### 2. 数据压缩
- 头像等大文件使用压缩格式
- 减少网络传输量

### 3. 异步处理
- 非关键操作异步执行
- 提高用户界面响应速度

## 监控和日志

### 1. 前端日志
```javascript
console.log('加载用户信息:', response)
console.log('保存用户信息:', saveData)
```

### 2. 后端日志
```java
log.info("获取用户个人信息请求")
log.info("更新用户个人信息请求，用户ID：{}", userId)
```

### 3. 数据库监控
- 监控用户表数据变化
- 记录用户操作历史
- 性能指标监控

## 扩展功能

### 1. 头像上传
- 支持文件上传到服务器
- 图片压缩和格式转换
- CDN加速

### 2. 数据导出
- 支持用户数据导出
- 多种格式支持（Excel、PDF等）

### 3. 操作历史
- 记录用户信息变更历史
- 支持数据回滚

## 总结

个人信息页面现在已经完全集成到数据库系统中，具有以下特点：

- ✅ **数据持久化**: 用户信息存储在数据库中
- ✅ **实时同步**: 前后端数据实时同步
- ✅ **安全可靠**: 完整的身份验证和权限控制
- ✅ **用户体验**: 快速加载和响应
- ✅ **错误处理**: 完善的错误处理机制
- ✅ **扩展性强**: 支持未来功能扩展

用户现在可以放心地编辑个人信息，数据会永久保存在数据库中，不会因为退出登录而丢失。 