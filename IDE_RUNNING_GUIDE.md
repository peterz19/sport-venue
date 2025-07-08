# IDE运行指南

本文档介绍如何在IDE（如IntelliJ IDEA）中直接运行Sport Venue微服务项目。

## 🎯 解决的问题

在IDE中直接运行微服务时，可能会遇到以下问题：
- 数据源配置错误：`Failed to configure a DataSource`
- 配置中心连接失败
- 服务注册失败

## 🔧 解决方案

我已经为每个服务创建了本地配置文件，并修改了启动类，使其在IDE中运行时自动使用`local` profile。

### 修改内容

1. **为每个服务创建了`application.yml`配置文件**
   - 包含完整的数据库连接配置
   - 包含Redis连接配置
   - 包含服务端口配置

2. **修改了所有服务的启动类**
   - 添加了`System.setProperty("spring.profiles.active", "local")`
   - 确保IDE运行时使用本地配置

## 🚀 运行步骤

### 1. 环境准备

确保本地环境已准备好：
```bash
# 检查环境
./check-local-env.sh
```

### 2. 在IDE中运行

#### 方式一：直接运行主类
1. 在IDE中找到各个服务的主类：
   - `UserApplication` - 用户服务
   - `VenueApplication` - 场馆服务
   - `PredictionApplication` - 预测服务
   - `SocialApplication` - 社交服务
   - `GatewayApplication` - 网关服务

2. 右键点击主类，选择"Run"或"Debug"

#### 方式二：设置VM参数
如果不想修改代码，也可以在IDE的运行配置中设置VM参数：
```
-Dspring.profiles.active=local
```

### 3. 运行顺序（推荐）

1. **配置中心**（可选）
   ```bash
   cd sport-venue-config
   mvn spring-boot:run -Dspring-boot.run.profiles=local
   ```

2. **注册中心**（可选）
   ```bash
   cd sport-venue-eureka
   mvn spring-boot:run
   ```

3. **业务服务**（按需运行）
   - 用户服务：`UserApplication`
   - 场馆服务：`VenueApplication`
   - 预测服务：`PredictionApplication`
   - 社交服务：`SocialApplication`
   - 网关服务：`GatewayApplication`

## 📊 服务端口

| 服务 | 端口 | 主类 |
|------|------|------|
| 用户服务 | 8081 | UserApplication |
| 场馆服务 | 8082 | VenueApplication |
| 预测服务 | 8083 | PredictionApplication |
| 社交服务 | 8084 | SocialApplication |
| 网关服务 | 8080 | GatewayApplication |

## 🔍 验证运行

### 1. 检查服务状态
```bash
# 测试用户服务
curl http://localhost:8081/actuator/health

# 测试场馆服务
curl http://localhost:8082/actuator/health

# 测试预测服务
curl http://localhost:8083/actuator/health

# 测试社交服务
curl http://localhost:8084/actuator/health

# 测试网关服务
curl http://localhost:8080/actuator/health
```

### 2. 查看日志
在IDE的控制台中查看服务启动日志，确认：
- 数据库连接成功
- Redis连接成功
- 服务启动成功

## 🛠️ 常见问题

### 1. 数据库连接失败
**错误信息**：`Failed to configure a DataSource`
**解决方案**：
- 确保MySQL已启动：`brew services start mysql`
- 确保数据库已创建：`mysql -u root -pAa123456 -e "CREATE DATABASE IF NOT EXISTS sport_venue;"`
- 检查密码是否正确：`Aa123456`

### 2. Redis连接失败
**错误信息**：`Redis connection failed`
**解决方案**：
- 确保Redis已启动：`brew services start redis`
- 检查Redis是否无密码运行：`redis-cli ping`

### 3. 端口被占用
**错误信息**：`Port 8081 is already in use`
**解决方案**：
```bash
# 查看端口占用
lsof -i :8081

# 杀掉占用进程
lsof -ti:8081 | xargs kill -9
```

### 4. 配置中心连接失败
**错误信息**：`Could not locate PropertySource`
**解决方案**：
- 这是正常的，因为IDE运行时使用本地配置文件
- 如果不需要配置中心，可以忽略此错误

## 📝 开发建议

### 1. 调试模式
在IDE中运行时，可以：
- 设置断点进行调试
- 查看变量值
- 单步执行代码

### 2. 热重载
如果项目配置了Spring Boot DevTools，可以：
- 修改代码后自动重启
- 实时查看日志变化

### 3. 日志配置
可以在`application.yml`中调整日志级别：
```yaml
logging:
  level:
    com.sportvenue: DEBUG
    org.springframework: INFO
```

## 🔄 切换运行模式

### 从IDE运行切换到脚本运行
1. 停止IDE中的服务
2. 运行脚本：`./start-local-services.sh`

### 从脚本运行切换到IDE运行
1. 停止脚本运行的服务：`./stop-services.sh`
2. 在IDE中直接运行主类

---

现在您可以在IDE中直接运行各个微服务了！每个服务都会自动使用本地配置，无需依赖配置中心。 