# 场馆服务 (Venue Service)

## 项目简介

场馆服务是体育场馆管理系统的核心模块，负责场馆信息管理、用户管理、权限控制等功能。支持多商户、多类型场馆管理，提供B端商户管理和C端用户查询的完整解决方案。

## 技术栈

- **Java**: 21
- **Spring Boot**: 3.2.0
- **Spring Cloud**: 2023.0.0
- **Spring Data JPA**: 数据持久化
- **MySQL**: 数据库
- **Redis**: 缓存
- **SpringDoc OpenAPI**: API文档
- **Lombok**: 代码简化
- **Maven**: 项目管理

## 功能特性

### 🏟️ 场馆管理
- 场馆CRUD操作
- 多类型场馆支持（室内、室外、机构、公园等）
- 多子类型支持（篮球、足球、网球、游泳、健身房等）
- 场馆状态管理（正常、停用、维护中）
- 场馆评分和评论系统
- 实时占用率监控

### 👥 用户管理
- 多角色用户系统（B端商户、B端员工、C端用户、管理员）
- 用户注册、登录、权限管理
- 商户管理
- 会员等级系统
- 积分系统

### 🔐 权限控制
- 基于角色的权限控制（RBAC）
- 细粒度权限管理
- 用户角色分配
- 权限验证

### 📊 数据统计
- 场馆使用统计
- 用户行为分析
- 收入统计
- 实时数据监控

## 项目结构

```
sport-venue-venue/
├── src/main/java/com/sportvenue/venue/
│   ├── config/                 # 配置类
│   │   ├── OpenApiConfig.java  # Swagger配置
│   │   ├── RedisConfig.java    # Redis配置
│   │   └── SecurityConfig.java # 安全配置
│   ├── controller/             # 控制器层
│   │   ├── VenueController.java        # 场馆管理
│   │   ├── BusinessVenueController.java # B端场馆管理
│   │   ├── CustomerVenueController.java # C端场馆查询
│   │   ├── UserController.java         # 用户管理
│   │   └── HealthController.java       # 健康检查
│   ├── entity/                 # 实体类
│   │   ├── Venue.java          # 场馆实体
│   │   ├── User.java           # 用户实体
│   │   ├── Role.java           # 角色实体
│   │   ├── Permission.java     # 权限实体
│   │   └── Merchant.java       # 商户实体
│   ├── repository/             # 数据访问层
│   │   ├── VenueRepository.java
│   │   ├── UserRepository.java
│   │   └── ...
│   ├── service/                # 业务逻辑层
│   │   ├── VenueService.java
│   │   ├── UserService.java
│   │   └── impl/
│   ├── dto/                    # 数据传输对象
│   │   ├── VenueDTO.java
│   │   └── VenueQueryDTO.java
│   └── websocket/              # WebSocket支持
│       └── OccupancyWebSocket.java
├── src/main/resources/
│   └── bootstrap.yml           # 配置文件
└── pom.xml                     # Maven配置
```

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+

### 1. 克隆项目

```bash
git clone <repository-url>
cd sport-venue-venue
```

### 2. 配置数据库

创建MySQL数据库：

```sql
CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 修改配置

编辑 `src/main/resources/bootstrap.yml`：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sport_venue?useSSL=false&characterEncoding=utf8
    username: your_username
    password: your_password
  redis:
    host: localhost
    port: 6379
```

### 4. 启动服务

```bash
# 编译项目
mvn clean compile

# 启动服务
mvn spring-boot:run
```

### 5. 访问API文档

启动成功后，访问：http://localhost:8082/swagger-ui.html

## API接口

### 场馆管理接口

#### 基础场馆管理 (`/venues`)
- `POST /venues` - 创建场馆
- `PUT /venues/{id}` - 更新场馆
- `DELETE /venues/{id}` - 删除场馆
- `GET /venues/{id}` - 获取场馆详情
- `GET /venues` - 分页查询场馆列表
- `GET /venues/merchant/{merchantId}` - 根据商户查询场馆
- `GET /venues/type/{type}` - 根据类型查询场馆
- `GET /venues/subtype/{subType}` - 根据子类型查询场馆
- `GET /venues/nearby` - 搜索附近场馆
- `GET /venues/popular` - 获取热门场馆

#### B端场馆管理 (`/business/venues`)
- 场馆统计信息
- 批量操作
- 数据导入导出
- 实时数据监控

#### C端场馆查询 (`/customer/venues`)
- 场馆列表查询
- 场馆详情查看
- 附近场馆搜索
- 推荐场馆

### 用户管理接口 (`/users`)
- `POST /users` - 创建用户
- `PUT /users/{id}` - 更新用户
- `DELETE /users/{id}` - 删除用户
- `GET /users/{id}` - 获取用户详情
- `GET /users` - 分页查询用户列表
- `POST /users/login` - 用户登录
- `POST /users/logout` - 用户登出
- `PUT /users/{id}/status` - 更新用户状态
- `PUT /users/{id}/roles` - 分配用户角色

### 健康检查接口 (`/health`)
- `GET /health` - 服务健康检查

## 数据库设计

### 主要数据表

1. **venues** - 场馆表
2. **users** - 用户表
3. **roles** - 角色表
4. **permissions** - 权限表
5. **merchants** - 商户表
6. **user_roles** - 用户角色关联表
7. **role_permissions** - 角色权限关联表

### 初始化数据

项目启动时会自动创建表结构，并插入基础数据：

- 系统管理员用户
- 基础角色和权限
- 示例场馆数据

## 配置说明

### 应用配置

```yaml
spring:
  application:
    name: venue-service
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: jdbc:mysql://localhost:3306/sport_venue
    username: root
    password: Aa123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
  redis:
    host: localhost
    port: 6379
    database: 0

server:
  port: 8082

# 场馆服务特有配置
venue:
  websocket:
    endpoint: /ws/occupancy
    allowed-origins: "*"
  occupancy:
    update-interval: 5000  # 5秒更新一次占用率
    cache-ttl: 300         # 5分钟缓存
```

### Swagger配置

```yaml
springdoc:
  api-docs:
    path: /api-docs
  swagger-ui:
    path: /swagger-ui.html
    tags-sorter: alpha
    operations-sorter: alpha
```

## 部署说明

### Docker部署

1. 构建镜像：

```bash
docker build -t sport-venue-venue .
```

2. 运行容器：

```bash
docker run -d \
  --name venue-service \
  -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=prod \
  sport-venue-venue
```

### 生产环境配置

1. 修改数据库连接配置
2. 配置Redis集群
3. 设置日志级别
4. 配置监控和告警

## 开发指南

### 添加新的API接口

1. 在对应的Controller中添加方法
2. 添加Swagger注解
3. 实现Service层逻辑
4. 添加单元测试

### 数据库变更

1. 创建实体类
2. 创建Repository接口
3. 更新数据库初始化脚本
4. 测试数据访问

### 代码规范

- 使用Lombok简化代码
- 添加完整的JavaDoc注释
- 遵循RESTful API设计规范
- 使用统一的异常处理

## 监控和日志

### 健康检查

- 访问 `/actuator/health` 查看服务健康状态
- 访问 `/actuator/info` 查看服务信息

### 日志配置

日志文件位置：`logs/venue-service.log`

日志级别配置：
```yaml
logging:
  level:
    com.sportvenue.venue: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
```

## 常见问题

### 1. 编译错误：找不到符号

**问题**：Lombok注解未生效
**解决**：
- 确保IDE安装了Lombok插件
- 检查Maven编译器插件配置
- 清理并重新编译项目

### 2. 数据库连接失败

**问题**：MySQL连接异常
**解决**：
- 检查数据库服务是否启动
- 验证连接配置是否正确
- 确认数据库用户权限

### 3. Redis连接失败

**问题**：Redis连接异常
**解决**：
- 检查Redis服务是否启动
- 验证Redis配置
- 确认网络连接

## 贡献指南

1. Fork项目
2. 创建功能分支
3. 提交代码
4. 创建Pull Request

## 许可证

本项目采用MIT许可证，详见 [LICENSE](LICENSE) 文件。

## 联系方式

- 项目维护者：SportVenue Team
- 邮箱：support@sportvenue.com
- 官网：https://www.sportvenue.com

---

**注意**：本文档会随着项目发展持续更新，请关注最新版本。 