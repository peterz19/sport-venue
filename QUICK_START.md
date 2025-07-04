# Sport Venue 微服务项目 - 快速启动指南

## 🚀 项目概述

这是一个基于Spring Boot 3.2 + Spring Cloud 2023的微服务项目，支持JDK 23，包含完整的微服务架构组件。

## 📋 前置要求

- **JDK 23** (已配置)
- **Maven 3.8+**
- **Docker Desktop** (用于基础设施服务)
- **Mac OS** (已适配)

## 🏗️ 项目结构

```
sport-venue/
├── config-repo/                   # 配置中心仓库
│   ├── application.yml            # 全局配置
│   ├── gateway.yml               # 网关配置
│   ├── user-service.yml          # 用户服务配置
│   ├── venue-service.yml         # 场馆服务配置
│   ├── prediction-service.yml    # 预测服务配置
│   └── social-service.yml        # 社交服务配置
├── sport-venue-common/           # 通用模块
├── sport-venue-eureka/           # 服务注册中心
├── sport-venue-config/           # 配置中心
├── sport-venue-gateway/          # API网关
├── sport-venue-user/             # 用户服务
├── sport-venue-venue/            # 场馆服务
├── sport-venue-prediction/       # 预测服务
├── sport-venue-social/           # 社交服务
├── start-services.sh             # 一键启动脚本
└── stop-services.sh              # 一键停止脚本
```

## ⚡ 快速启动

### 1. 一键启动所有服务

```bash
# 启动所有微服务
./start-services.sh
```

### 2. 手动启动（推荐开发时使用）

```bash
# 1. 启动基础设施
docker-compose up -d

# 2. 启动配置中心
cd sport-venue-config && mvn spring-boot:run

# 3. 启动注册中心
cd sport-venue-eureka && mvn spring-boot:run

# 4. 启动业务服务（新终端窗口）
cd sport-venue-user && mvn spring-boot:run
cd sport-venue-venue && mvn spring-boot:run
cd sport-venue-prediction && mvn spring-boot:run
cd sport-venue-social && mvn spring-boot:run

# 5. 启动网关
cd sport-venue-gateway && mvn spring-boot:run
```

## 🌐 服务访问地址

| 服务 | 地址 | 说明 |
|------|------|------|
| 配置中心 | http://localhost:8888 | Spring Cloud Config |
| 注册中心 | http://localhost:8761 | Eureka Dashboard |
| 网关服务 | http://localhost:8080 | API Gateway |
| 用户服务 | http://localhost:8081 | 用户管理 |
| 场馆服务 | http://localhost:8082 | 场馆管理 |
| 预测服务 | http://localhost:8083 | 人数预测 |
| 社交服务 | http://localhost:8084 | 社交功能 |

## 🔧 开发指南

### 添加新服务

1. 在父POM中添加模块
2. 创建服务目录结构
3. 配置bootstrap.yml
4. 在config-repo中添加配置文件

### 添加新依赖

在对应服务的`pom.xml`中添加依赖，父POM已管理版本。

### 配置管理

- 全局配置：`config-repo/application.yml`
- 服务配置：`config-repo/{service-name}.yml`
- 本地配置：`src/main/resources/bootstrap.yml`

## 🛠️ 常用命令

```bash
# 编译整个项目
mvn clean compile

# 打包整个项目
mvn clean package

# 启动单个服务
cd sport-venue-user && mvn spring-boot:run

# 查看服务日志
tail -f logs/user.log

# 停止所有服务
./stop-services.sh
```

## 📝 注意事项

1. **启动顺序**：配置中心 → 注册中心 → 业务服务 → 网关
2. **端口冲突**：确保端口未被占用
3. **Docker状态**：确保Docker Desktop正在运行
4. **JDK版本**：项目配置为JDK 23，请确保环境匹配

## 🐛 故障排除

### 常见问题

1. **端口被占用**
   ```bash
   lsof -ti:8080 | xargs kill -9
   ```

2. **Docker容器启动失败**
   ```bash
   docker-compose down
   docker-compose up -d
   ```

3. **服务注册失败**
   - 检查Eureka是否正常启动
   - 检查网络连接
   - 查看服务日志

### 日志查看

```bash
# 查看所有日志
ls -la logs/

# 查看特定服务日志
tail -f logs/user.log
```

## 📚 下一步

1. 在`controller/`目录下添加REST接口
2. 在`service/`目录下实现业务逻辑
3. 在`repository/`目录下添加数据访问层
4. 配置数据库连接和JPA实体

---

**🎉 项目初始化完成！现在可以开始开发业务代码了。** 