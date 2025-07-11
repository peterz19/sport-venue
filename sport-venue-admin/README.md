# 运动场馆管理系统 - B端管理后台

## 项目简介

B端管理后台是运动场馆管理系统的管理端，提供完整的场馆管理功能，包括场馆的增删改查、数据统计、用户管理等。

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
- 场馆列表展示
- 场馆信息编辑
- 场馆状态管理
- 场馆删除操作
- 场馆搜索筛选

### 📊 数据看板
- 场馆统计概览
- 场馆类型分布图表
- 场馆状态分布图表
- 热门场馆排行

### 🔍 搜索功能
- 按场馆名称搜索
- 按场馆类型筛选
- 按场馆状态筛选
- 分页展示

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

访问地址: http://localhost:3000

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
sport-venue-admin/
├── src/
│   ├── api/              # API接口
│   │   └── venue.js      # 场馆相关API
│   ├── components/       # 公共组件
│   │   └── Layout.vue    # 布局组件
│   ├── router/           # 路由配置
│   │   └── index.js      # 路由定义
│   ├── utils/            # 工具函数
│   │   └── request.js    # HTTP请求封装
│   ├── views/            # 页面组件
│   │   ├── Dashboard.vue # 数据看板
│   │   └── venue/        # 场馆相关页面
│   │       ├── VenueList.vue    # 场馆列表
│   │       ├── VenueForm.vue    # 场馆表单
│   │       └── VenueDetail.vue  # 场馆详情
│   ├── App.vue           # 根组件
│   └── main.js           # 入口文件
├── index.html            # HTML模板
├── vite.config.js        # Vite配置
└── package.json          # 项目配置
```

## API接口

### 场馆管理接口

- `GET /api/venues` - 获取场馆列表
- `GET /api/venues/{id}` - 获取场馆详情
- `POST /api/venues` - 创建场馆
- `PUT /api/venues/{id}` - 更新场馆
- `DELETE /api/venues/{id}` - 删除场馆
- `PUT /api/venues/{id}/status` - 更新场馆状态
- `PUT /api/venues/{id}/occupancy` - 更新场馆使用人数
- `PUT /api/venues/{id}/rating` - 更新场馆评分

### 数据接口

- `GET /api/venues/types` - 获取场馆类型列表
- `GET /api/venues/statuses` - 获取场馆状态列表

## 开发指南

### 添加新页面

1. 在 `src/views/` 目录下创建页面组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在 `src/components/Layout.vue` 中添加菜单项

### 添加新API

1. 在 `src/api/` 目录下创建API文件
2. 在 `src/utils/request.js` 中配置请求拦截器（如需要）

### 样式规范

- 使用 Element Plus 组件库
- 遵循 BEM 命名规范
- 使用 scoped 样式避免样式污染

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

## 常见问题

### Q: 启动时提示端口被占用？

A: 可以修改 `vite.config.js` 中的端口配置：

```javascript
server: {
  port: 3001  // 修改为其他可用端口
}
```

### Q: API请求失败？

A: 检查以下几点：
1. 后端服务是否启动
2. API地址是否正确
3. 网络连接是否正常

### Q: 页面样式异常？

A: 检查以下几点：
1. Element Plus 是否正确引入
2. 浏览器兼容性
3. CSS样式是否冲突

## 更新日志

### v1.0.0 (2024-01-01)
- 初始版本发布
- 实现基础场馆管理功能
- 添加数据看板
- 集成Element Plus UI组件库

## 贡献指南

1. Fork 项目
2. 创建功能分支
3. 提交代码
4. 创建 Pull Request

## 许可证

MIT License 