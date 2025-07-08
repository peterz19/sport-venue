# 本地环境设置指南

本文档介绍如何在本地环境中运行Sport Venue微服务项目，无需使用Docker。

## 前置要求

### 1. Java环境
- JDK 17 或更高版本
- Maven 3.6 或更高版本

### 2. 数据库
- MySQL 8.0 或更高版本
- 端口：3306
- 用户名：root
- 密码：Aa123456
- 数据库名：sport_venue

### 3. Redis
- Redis 6.0 或更高版本
- 端口：6379
- 无密码

### 4. Consul（可选）
- Consul 1.15 或更高版本
- 端口：8500

## 安装步骤

### 1. 安装MySQL

#### macOS (使用Homebrew)
```bash
# 安装MySQL
brew install mysql

# 启动MySQL服务
brew services start mysql

# 设置root密码
mysql_secure_installation
```

#### 查找MySQL路径
如果安装后无法使用`mysql`命令，可以运行：
```bash
./find-mysql.sh
```

这会显示MySQL的安装路径，然后可以：
1. 将路径添加到PATH中
2. 或使用完整路径运行MySQL命令

#### 创建数据库
```bash
# 如果mysql命令在PATH中
mysql -u root -pAa123456 -e "CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 或者使用完整路径（根据安装位置选择）
# Homebrew安装
/opt/homebrew/bin/mysql -u root -pAa123456 -e "CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

# 官方安装包
/usr/local/mysql/bin/mysql -u root -pAa123456 -e "CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

### 2. 安装Redis

#### macOS (使用Homebrew)
```bash
# 安装Redis
brew install redis

# 启动Redis服务
brew services start redis

# 启动Redis（无密码）
brew services start redis
```

### 3. 安装Consul（可选）

#### macOS (使用Homebrew)
```bash
# 安装Consul
brew install consul

# 启动Consul服务
consul agent -dev -client=0.0.0.0
```

## 配置文件说明

### 本地配置文件结构
```
config-repo/
├── application-local.yml          # 全局本地配置
├── user-service-local.yml         # 用户服务本地配置
├── venue-service-local.yml        # 场馆服务本地配置
├── prediction-service-local.yml   # 预测服务本地配置
├── social-service-local.yml       # 社交服务本地配置
└── gateway-local.yml              # 网关服务本地配置
```

### 主要配置变更
1. **数据库连接**：从 `localhost:3307` 改为 `localhost:3306`
2. **Redis连接**：从 `redis` 改为 `localhost`
3. **Consul连接**：从 `consul` 改为 `localhost`

## 启动服务

### 1. 使用脚本启动（推荐）
```bash
# 给脚本执行权限
chmod +x start-local-services.sh

# 启动所有服务
./start-local-services.sh
```

### 2. 手动启动
```bash
# 1. 启动配置中心
cd sport-venue-config
mvn spring-boot:run -Dspring-boot.run.profiles=local

# 2. 启动注册中心
cd sport-venue-eureka
mvn spring-boot:run

# 3. 启动业务服务
cd sport-venue-user
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd sport-venue-venue
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd sport-venue-prediction
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd sport-venue-social
mvn spring-boot:run -Dspring-boot.run.profiles=local

cd sport-venue-gateway
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 配置中心 | http://localhost:8888 | Spring Cloud Config |
| 注册中心 | http://localhost:8761 | Eureka Server |
| 网关服务 | http://localhost:8080 | Spring Cloud Gateway |
| 用户服务 | http://localhost:8081 | 用户管理服务 |
| 场馆服务 | http://localhost:8082 | 场馆管理服务 |
| 预测服务 | http://localhost:8083 | 预测分析服务 |
| 社交服务 | http://localhost:8084 | 社交功能服务 |

## 停止服务

```bash
# 使用脚本停止
./stop-services.sh

# 或手动停止
pkill -f "spring-boot:run"
```

## 常见问题

### 1. 端口被占用
```bash
# 查看端口占用
lsof -i :8080

# 杀掉占用进程
lsof -ti:8080 | xargs kill -9
```

### 2. 数据库连接失败
- 检查MySQL服务是否启动
- 确认数据库用户名密码正确
- 确认数据库sport_venue已创建

### 3. Redis连接失败
- 检查Redis服务是否启动
- 确认Redis无密码配置

### 4. 配置中心无法访问
- 确认配置中心已启动
- 检查配置文件路径是否正确

## 日志查看

所有服务的日志文件保存在 `logs/` 目录下：
- `config.log` - 配置中心日志
- `eureka.log` - 注册中心日志
- `user.log` - 用户服务日志
- `venue.log` - 场馆服务日志
- `prediction.log` - 预测服务日志
- `social.log` - 社交服务日志
- `gateway.log` - 网关服务日志

## 开发建议

1. **IDE集成**：在IDE中直接运行各个服务，便于调试
2. **热重载**：使用Spring Boot DevTools实现代码热重载
3. **配置管理**：根据开发环境调整配置文件
4. **日志级别**：在开发环境中设置更详细的日志级别 