# 本地运行方案总结

## 📋 概述

我已经为您的Sport Venue微服务项目创建了完整的本地运行方案，无需依赖Docker容器。这个方案包括：

1. **本地配置文件** - 适配本地环境的配置
2. **启动脚本** - 自动化启动所有服务
3. **环境检查** - 验证本地环境是否满足要求
4. **测试脚本** - 验证服务是否正常运行
5. **详细文档** - 完整的设置和使用指南

## 🗂️ 新增文件列表

### 启动脚本
- `start-local-services.sh` - 本地服务启动脚本
- `check-local-env.sh` - 本地环境检查脚本
- `test-services.sh` - 服务状态测试脚本

### 配置文件
- `config-repo/application-local.yml` - 全局本地配置
- `config-repo/user-service-local.yml` - 用户服务本地配置
- `config-repo/venue-service-local.yml` - 场馆服务本地配置
- `config-repo/prediction-service-local.yml` - 预测服务本地配置
- `config-repo/social-service-local.yml` - 社交服务本地配置
- `config-repo/gateway-local.yml` - 网关服务本地配置

### 服务配置文件
- `sport-venue-config/src/main/resources/application-local.yml` - 配置中心本地配置
- `sport-venue-user/src/main/resources/bootstrap-local.yml` - 用户服务本地配置
- `sport-venue-venue/src/main/resources/bootstrap-local.yml` - 场馆服务本地配置
- `sport-venue-prediction/src/main/resources/bootstrap-local.yml` - 预测服务本地配置
- `sport-venue-social/src/main/resources/bootstrap-local.yml` - 社交服务本地配置
- `sport-venue-gateway/src/main/resources/bootstrap-local.yml` - 网关服务本地配置

### 文档
- `LOCAL_SETUP.md` - 本地环境设置详细指南
- `LOCAL_RUNNING_SUMMARY.md` - 本总结文档

## 🔧 主要配置变更

### 1. 数据库连接
- **原配置**: `localhost:3307` (Docker端口映射)
- **新配置**: `localhost:3306` (本地MySQL端口)

### 2. Redis连接
- **原配置**: `host: redis` (Docker容器名)
- **新配置**: `host: localhost` (本地Redis)

### 3. Consul连接
- **原配置**: `host: consul` (Docker容器名)
- **新配置**: `host: localhost` (本地Consul)

### 4. Spring Boot配置
- 使用 `spring.config.activate.on-profile: local` 替代 `spring.profiles.active`
- 适配Spring Boot 3.x的新配置方式

## 🚀 使用方法

### 1. 环境准备
```bash
# 安装MySQL (macOS)
brew install mysql
brew services start mysql

# 安装Redis (macOS)
brew install redis
brew services start redis

# 创建数据库
mysql -u root -pAa123456 -e "CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 检查环境
```bash
./check-local-env.sh
```

### 3. 启动服务
```bash
./start-local-services.sh
```

### 4. 测试服务
```bash
./test-services.sh
```

### 5. 停止服务
```bash
./stop-services.sh
```

## 📊 服务端口分配

| 服务 | 端口 | 说明 |
|------|------|------|
| 配置中心 | 8888 | Spring Cloud Config |
| 注册中心 | 8761 | Eureka Server |
| 网关服务 | 8080 | Spring Cloud Gateway |
| 用户服务 | 8081 | 用户管理服务 |
| 场馆服务 | 8082 | 场馆管理服务 |
| 预测服务 | 8083 | 预测分析服务 |
| 社交服务 | 8084 | 社交功能服务 |

## 🔍 环境要求

### 必需服务
- **MySQL 8.0+**: 端口3306，用户名root，密码Aa123456
- **Redis 6.0+**: 端口6379，无密码

### 可选服务
- **Consul 1.15+**: 端口8500（用于服务发现）

### 开发环境
- **JDK 17+**: Java运行环境
- **Maven 3.6+**: 项目构建工具

## 🛠️ 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   lsof -ti:8080 | xargs kill -9
   ```

2. **MySQL连接失败**
   - 检查MySQL服务是否启动
   - 确认用户名密码正确
   - 确认数据库已创建

3. **Redis连接失败**
   - 检查Redis服务是否启动
   - 确认Redis无密码配置

4. **配置中心无法访问**
   - 确认配置中心已启动
   - 检查配置文件路径

### 日志查看
所有服务日志保存在 `logs/` 目录下：
- `config.log` - 配置中心日志
- `eureka.log` - 注册中心日志
- `user.log` - 用户服务日志
- `venue.log` - 场馆服务日志
- `prediction.log` - 预测服务日志
- `social.log` - 社交服务日志
- `gateway.log` - 网关服务日志

## 🎯 优势

### 本地运行的优势
1. **开发效率高** - 无需等待Docker容器启动
2. **调试方便** - 可以直接在IDE中调试
3. **资源占用少** - 不需要Docker引擎
4. **配置灵活** - 可以随时修改配置
5. **热重载支持** - 支持代码热重载

### 与Docker运行的区别
| 特性 | 本地运行 | Docker运行 |
|------|----------|------------|
| 启动速度 | 快 | 慢 |
| 资源占用 | 少 | 多 |
| 环境隔离 | 无 | 有 |
| 部署一致性 | 依赖本地环境 | 一致性好 |
| 调试便利性 | 高 | 低 |

## 📝 注意事项

1. **环境依赖** - 需要本地安装MySQL、Redis等服务
2. **端口冲突** - 确保所需端口未被占用
3. **配置管理** - 本地配置与Docker配置分离
4. **数据持久化** - 本地数据不会自动备份
5. **团队协作** - 需要统一本地环境配置

## 🔄 切换运行模式

### 从Docker切换到本地
1. 停止Docker服务：`docker-compose down`
2. 启动本地服务：`./start-local-services.sh`

### 从本地切换到Docker
1. 停止本地服务：`./stop-services.sh`
2. 启动Docker服务：`docker-compose up -d && ./start-services.sh`

---

现在您可以选择使用本地运行或Docker运行两种方式来启动您的微服务项目。本地运行更适合开发和调试，而Docker运行更适合生产环境部署。 