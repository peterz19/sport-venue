# 运动场馆管理系统 - 商户端

## 项目简介

商户端是运动场馆管理系统的商户管理界面，为场馆商户提供专属的管理功能，包括场馆信息管理、使用情况监控、数据统计等。

## 技术栈

- **前端框架**: Vue 3
- **UI组件库**: Element Plus
- **路由管理**: Vue Router 4
- **状态管理**: Pinia
- **构建工具**: Vite
- **图表库**: ECharts
- **HTTP客户端**: Axios

## 功能特性

### 🏟️ 场馆管理
- 我的场馆列表
- 场馆详情查看
- 场馆信息编辑
- 场馆状态更新
- 使用人数管理

### 📊 数据看板
- 场馆统计概览
- 使用率分析图表
- 评分分布统计
- 实时数据监控

### 🔐 商户认证
- 商户登录系统
- 身份验证
- 数据隔离
- 权限控制

### 📱 响应式设计
- 移动端适配
- 桌面端优化
- 多设备支持

## 快速开始

### 环境要求

- Node.js >= 16.0.0
- npm >= 8.0.0

### 安装依赖

```bash
npm install
```

### 启动开发服务器

```bash
npm run dev
```

访问地址: http://localhost:3001

### 构建生产版本

```bash
npm run build
```

### 预览生产版本

```bash
npm run preview
```

## 项目结构

```
sport-venue-merchant/
├── src/
│   ├── api/              # API接口
│   │   └── index.js      # 商户相关API
│   ├── components/       # 公共组件
│   │   └── Layout.vue    # 布局组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── utils/            # 工具函数
│   │   └── request.js    # HTTP请求封装
│   ├── views/            # 页面组件
│   │   ├── login/        # 登录相关页面
│   │   │   └── Login.vue # 登录页面
│   │   ├── dashboard/    # 数据看板
│   │   │   └── Dashboard.vue # 商户数据看板
│   │   └── venue/        # 场馆相关页面
│   │       ├── VenueList.vue    # 场馆列表
│   │       └── VenueDetail.vue  # 场馆详情
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── vite.config.js        # Vite配置
└── package.json          # 项目配置
```

## API接口

### 商户认证接口

- `POST /api/auth/merchant/login` - 商户登录
- `POST /api/auth/merchant/logout` - 商户登出
- `GET /api/auth/merchant/info` - 获取商户信息

### 场馆管理接口

- `GET /api/venues/merchant` - 获取商户的场馆列表
- `GET /api/venues/{id}` - 获取场馆详情
- `PUT /api/venues/{id}` - 更新场馆信息
- `PUT /api/venues/{id}/status` - 更新场馆状态
- `PUT /api/venues/{id}/occupancy` - 更新场馆使用人数
- `PUT /api/venues/{id}/rating` - 更新场馆评分

### 数据统计接口

- `GET /api/venues/merchant/stats` - 获取商户场馆统计数据

## 使用说明

### 商户登录

1. 访问商户端登录页面
2. 输入商户ID和密码
3. 点击登录按钮

### 测试账号

- **商户ID**: merchant001
- **密码**: 123456

### 主要功能

1. **数据看板**: 查看场馆统计数据和图表
2. **场馆管理**: 管理自己的场馆信息
3. **人数更新**: 实时更新场馆使用人数
4. **状态管理**: 管理场馆营业状态

## 开发指南

### 添加新页面

1. 在 `src/views/` 目录下创建页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/components/Layout.vue` 中添加菜单项

### 添加新API

1. 在 `src/api/index.js` 中添加API方法
2. 确保API请求包含商户认证信息

### 权限控制

- 所有需要认证的页面都添加 `requiresAuth: true` 元信息
- 在路由守卫中检查商户登录状态
- API请求自动添加商户ID头部

## 部署说明

### 开发环境

```bash
npm run dev
```

### 生产环境

```bash
npm run build
```

构建完成后，将 `dist` 目录部署到Web服务器。

## 安全特性

### 数据隔离

- 商户只能访问自己的场馆数据
- API请求自动添加商户ID验证
- 前端路由守卫防止越权访问

### 认证机制

- 基于localStorage的会话管理
- 自动登录状态检查
- 登出时清除本地数据

## 常见问题

### Q: 登录失败？

A: 检查以下几点：
1. 商户ID和密码是否正确
2. 后端服务是否启动
3. 网络连接是否正常

### Q: 看不到场馆数据？

A: 检查以下几点：
1. 是否已正确登录
2. 商户ID是否正确
3. 后端是否有对应的场馆数据

### Q: 页面样式异常？

A: 检查以下几点：
1. Element Plus 是否正确引入
2. 浏览器兼容性
3. CSS样式是否冲突

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现商户登录功能
- 添加场馆管理功能
- 集成数据看板
- 实现数据隔离

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License 