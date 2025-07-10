# 场馆服务 (Sport Venue Service)

## 概述

场馆服务是体育场馆管理系统的核心服务，提供多商户、多类型场馆的完整管理功能。支持室内外场馆、机构场馆、公园场馆等多种类型，具备预约管理、打卡积分、实时推送等核心功能。

## 功能特性

### 🏟️ 多类型场馆支持
- **室内场馆**: 篮球场、羽毛球场、乒乓球室等
- **室外场馆**: 足球场、网球场、跑道等
- **机构场馆**: 健身房、游泳馆、瑜伽馆等
- **公园场馆**: 免费运动场地、健身器材区等
- **付费/免费场馆**: 支持不同收费模式

### 🏪 多商户管理
- 支持多个商户独立管理各自的场馆
- 商户权限隔离，数据安全
- 商户统计报表和数据分析

### 📅 预约管理系统
- 灵活的预约时间设置
- 预约冲突检测
- 预约状态跟踪（待确认、已确认、已取消、已完成）
- 支付集成（微信、支付宝、现金、刷卡、积分抵扣）

### 💰 复杂价格策略
- 时段定价（高峰价、低峰价）
- 会员价格和VIP价格
- 节假日价格调整
- 套餐价格和按天/按小时计费
- 优先级价格策略

### 📍 打卡积分系统
- 多种打卡方式（二维码、NFC、人脸识别）
- 积分奖励机制
- 连续打卡天数统计
- 使用时长记录

### 📊 实时数据推送
- WebSocket实时推送场馆使用情况
- 在线用户数实时更新
- 预约状态变更通知

### 🔐 权限控制
- 多角色权限管理
- 商户管理员权限
- 场馆管理员权限
- 用户权限控制

## 技术架构

### 数据模型

#### 核心实体
- **Venue**: 场馆实体，支持多类型、多商户
- **VenuePrice**: 价格策略实体，支持复杂定价
- **VenueReservation**: 预约实体，支持状态跟踪
- **VenueCheckIn**: 打卡实体，支持积分统计

#### 数据关系
```
Venue (1) ←→ (N) VenuePrice
Venue (1) ←→ (N) VenueReservation
Venue (1) ←→ (N) VenueCheckIn
```

### 服务架构

#### 分层设计
- **Controller层**: RESTful API接口
- **Service层**: 业务逻辑处理
- **Repository层**: 数据访问层
- **Entity层**: 数据模型层

#### 核心服务
- **VenueService**: 场馆管理服务
- **VenuePriceService**: 价格策略服务
- **VenueReservationService**: 预约管理服务
- **VenueCheckInService**: 打卡积分服务

## API接口

### 场馆管理接口

#### 基础CRUD
- `POST /api/venues` - 创建场馆
- `PUT /api/venues/{id}` - 更新场馆
- `DELETE /api/venues/{id}` - 删除场馆
- `GET /api/venues/{id}` - 查询场馆详情

#### 查询接口
- `GET /api/venues` - 分页查询场馆列表
- `GET /api/venues/merchant/{merchantId}` - 查询商户场馆
- `GET /api/venues/type/{type}` - 按类型查询
- `GET /api/venues/subtype/{subType}` - 按子类型查询
- `GET /api/venues/status/{status}` - 按状态查询
- `GET /api/venues/nearby` - 搜索附近场馆
- `GET /api/venues/popular` - 查询热门场馆

#### 统计接口
- `GET /api/venues/statistics/{merchantId}` - 场馆统计
- `GET /api/venues/statistics/{merchantId}/types` - 类型统计
- `GET /api/venues/statistics/{merchantId}/subtypes` - 子类型统计

#### 枚举接口
- `GET /api/venues/enums/types` - 场馆类型枚举
- `GET /api/venues/enums/subtypes` - 场馆子类型枚举
- `GET /api/venues/enums/statuses` - 场馆状态枚举

### 预约管理接口

#### 预约操作
- `POST /api/reservations` - 创建预约
- `PUT /api/reservations/{id}` - 更新预约
- `DELETE /api/reservations/{id}` - 取消预约
- `GET /api/reservations/{id}` - 查询预约详情

#### 预约查询
- `GET /api/reservations/venue/{venueId}` - 场馆预约列表
- `GET /api/reservations/user/{userId}` - 用户预约列表
- `GET /api/reservations/today/{venueId}` - 今日预约
- `GET /api/reservations/upcoming/{venueId}` - 即将到期预约

### 打卡管理接口

#### 打卡操作
- `POST /api/checkins` - 用户打卡
- `POST /api/checkins/checkout` - 用户离场
- `GET /api/checkins/{id}` - 查询打卡记录

#### 打卡统计
- `GET /api/checkins/user/{userId}/statistics` - 用户打卡统计
- `GET /api/checkins/venue/{venueId}/statistics` - 场馆打卡统计
- `GET /api/checkins/venue/{venueId}/online-users` - 在线用户数

## 数据库设计

### 场馆表 (venues)
```sql
CREATE TABLE venues (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    description VARCHAR(500) COMMENT '场馆描述',
    type VARCHAR(20) NOT NULL COMMENT '场馆类型',
    sub_type VARCHAR(20) NOT NULL COMMENT '场馆子类型',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    address VARCHAR(200) NOT NULL COMMENT '场馆地址',
    longitude DECIMAL(10,7) COMMENT '经度',
    latitude DECIMAL(10,7) COMMENT '纬度',
    phone VARCHAR(20) COMMENT '联系电话',
    open_time VARCHAR(10) COMMENT '营业开始时间',
    close_time VARCHAR(10) COMMENT '营业结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '场馆状态',
    capacity INT COMMENT '场馆容量',
    current_occupancy INT DEFAULT 0 COMMENT '当前使用人数',
    area DECIMAL(10,2) COMMENT '场馆面积',
    facilities VARCHAR(500) COMMENT '场馆设施',
    images TEXT COMMENT '场馆图片',
    tags TEXT COMMENT '场馆标签',
    rating DECIMAL(3,2) DEFAULT 0 COMMENT '评分',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    reservation_enabled BOOLEAN DEFAULT TRUE COMMENT '是否支持预约',
    check_in_enabled BOOLEAN DEFAULT TRUE COMMENT '是否支持打卡',
    points_enabled BOOLEAN DEFAULT TRUE COMMENT '是否支持积分',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_type (type),
    INDEX idx_status (status),
    INDEX idx_location (longitude, latitude)
);
```

### 价格策略表 (venue_prices)
```sql
CREATE TABLE venue_prices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    name VARCHAR(100) NOT NULL COMMENT '价格策略名称',
    type VARCHAR(20) NOT NULL COMMENT '价格类型',
    base_price DECIMAL(10,2) NOT NULL COMMENT '基础价格',
    member_price DECIMAL(10,2) COMMENT '会员价格',
    vip_price DECIMAL(10,2) COMMENT 'VIP价格',
    holiday_price DECIMAL(10,2) COMMENT '节假日价格',
    time_slot_start VARCHAR(10) COMMENT '时段开始时间',
    time_slot_end VARCHAR(10) COMMENT '时段结束时间',
    weekdays VARCHAR(20) COMMENT '适用星期',
    valid_from DATETIME COMMENT '有效期开始',
    valid_to DATETIME COMMENT '有效期结束',
    min_duration INT COMMENT '最小使用时长',
    max_duration INT COMMENT '最大使用时长',
    enabled BOOLEAN DEFAULT TRUE COMMENT '是否启用',
    priority INT DEFAULT 0 COMMENT '优先级',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    INDEX idx_venue_id (venue_id),
    INDEX idx_type (type),
    INDEX idx_enabled (enabled)
);
```

### 预约表 (venue_reservations)
```sql
CREATE TABLE venue_reservations (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    reservation_no VARCHAR(50) NOT NULL UNIQUE COMMENT '预约编号',
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户姓名',
    user_phone VARCHAR(20) COMMENT '用户手机号',
    start_time DATETIME COMMENT '预约开始时间',
    end_time DATETIME COMMENT '预约结束时间',
    duration INT COMMENT '使用时长',
    people_count INT DEFAULT 1 COMMENT '预约人数',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '预约状态',
    type VARCHAR(20) NOT NULL DEFAULT 'NORMAL' COMMENT '预约类型',
    unit_price DECIMAL(10,2) COMMENT '单价',
    total_price DECIMAL(10,2) COMMENT '总价',
    discount_amount DECIMAL(10,2) DEFAULT 0 COMMENT '折扣金额',
    actual_amount DECIMAL(10,2) COMMENT '实付金额',
    payment_status VARCHAR(20) NOT NULL DEFAULT 'UNPAID' COMMENT '支付状态',
    payment_method VARCHAR(20) COMMENT '支付方式',
    payment_time DATETIME COMMENT '支付时间',
    transaction_no VARCHAR(100) COMMENT '交易流水号',
    remark VARCHAR(500) COMMENT '备注',
    cancel_reason VARCHAR(200) COMMENT '取消原因',
    cancel_time DATETIME COMMENT '取消时间',
    cancel_by BIGINT COMMENT '取消人ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    INDEX idx_venue_id (venue_id),
    INDEX idx_user_id (user_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
);
```

### 打卡表 (venue_check_ins)
```sql
CREATE TABLE venue_check_ins (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    check_in_no VARCHAR(50) NOT NULL UNIQUE COMMENT '打卡编号',
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    user_name VARCHAR(50) COMMENT '用户姓名',
    reservation_id BIGINT COMMENT '预约ID',
    check_in_time DATETIME COMMENT '打卡时间',
    type VARCHAR(20) NOT NULL DEFAULT 'CHECK_IN' COMMENT '打卡类型',
    method VARCHAR(20) NOT NULL DEFAULT 'QR_CODE' COMMENT '打卡方式',
    location VARCHAR(100) COMMENT '打卡位置',
    device_id VARCHAR(100) COMMENT '设备ID',
    device_type VARCHAR(20) COMMENT '设备类型',
    earned_points INT DEFAULT 0 COMMENT '获得积分',
    consecutive_days INT DEFAULT 0 COMMENT '连续打卡天数',
    total_check_ins INT DEFAULT 0 COMMENT '总打卡次数',
    duration_minutes INT DEFAULT 0 COMMENT '使用时长',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by BIGINT,
    update_by BIGINT,
    INDEX idx_venue_id (venue_id),
    INDEX idx_user_id (user_id),
    INDEX idx_type (type),
    INDEX idx_check_in_time (check_in_time)
);
```

## 部署说明

### 环境要求
- JDK 17+
- MySQL 8.0+
- Redis 6.0+
- Spring Boot 3.0+

### 配置说明

#### 数据库配置
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/sport_venue?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: 
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
```

#### Redis配置
```yaml
spring:
  redis:
    host: localhost
    port: 6379
    password: 
    database: 0
    timeout: 3000ms
    lettuce:
      pool:
        max-active: 8
        max-wait: -1ms
        max-idle: 8
        min-idle: 0
```

### 启动步骤
1. 确保MySQL和Redis服务已启动
2. 创建数据库：`CREATE DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;`
3. 修改配置文件中的数据库连接信息
4. 启动场馆服务：`mvn spring-boot:run`

## 开发指南

### 添加新的场馆类型
1. 在`Venue.VenueType`枚举中添加新类型
2. 在`Venue.VenueSubType`枚举中添加对应的子类型
3. 更新相关的业务逻辑和前端界面

### 扩展价格策略
1. 在`VenuePrice.PriceType`枚举中添加新策略类型
2. 在`VenuePriceService`中实现相应的价格计算逻辑
3. 更新价格策略的验证和查询逻辑

### 自定义打卡方式
1. 在`VenueCheckIn.CheckInMethod`枚举中添加新方式
2. 在`VenueCheckInService`中实现相应的打卡逻辑
3. 更新打卡验证和统计逻辑

## 监控和日志

### 日志配置
```yaml
logging:
  level:
    com.sportvenue.venue: DEBUG
    org.springframework.web: INFO
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  file:
    name: logs/venue-service.log
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### 健康检查
- 健康检查端点：`GET /actuator/health`
- 应用信息：`GET /actuator/info`
- 指标监控：`GET /actuator/metrics`

## 常见问题

### Q: 如何添加新的场馆类型？
A: 在`Venue.VenueType`和`Venue.VenueSubType`枚举中添加新类型，然后更新相关的业务逻辑。

### Q: 如何自定义价格策略？
A: 在`VenuePrice.PriceType`枚举中添加新策略，并在`VenuePriceService`中实现相应的计算逻辑。

### Q: 如何扩展打卡方式？
A: 在`VenueCheckIn.CheckInMethod`枚举中添加新方式，并在`VenueCheckInService`中实现相应的逻辑。

### Q: 如何处理高并发预约？
A: 使用Redis分布式锁和数据库事务来确保预约的一致性和并发安全。

## 贡献指南

1. Fork 项目
2. 创建功能分支：`git checkout -b feature/new-feature`
3. 提交更改：`git commit -am 'Add new feature'`
4. 推送分支：`git push origin feature/new-feature`
5. 提交 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者：[维护者姓名]
- 邮箱：[邮箱地址]
- 项目地址：[项目GitHub地址] 