-- 创建数据库
CREATE DATABASE IF NOT EXISTS sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE sport_venue;

-- 创建场馆表
CREATE TABLE IF NOT EXISTS venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    description VARCHAR(500) COMMENT '场馆描述',
    type VARCHAR(20) NOT NULL COMMENT '场馆类型',
    sub_type VARCHAR(20) NOT NULL COMMENT '场馆子类型',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    address VARCHAR(200) NOT NULL COMMENT '场馆地址',
    longitude DECIMAL(10, 7) COMMENT '经度',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    phone VARCHAR(20) COMMENT '联系电话',
    open_time VARCHAR(10) COMMENT '营业时间开始',
    close_time VARCHAR(10) COMMENT '营业时间结束',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '场馆状态',
    capacity INT COMMENT '场馆容量',
    current_occupancy INT DEFAULT 0 COMMENT '当前使用人数',
    area DECIMAL(10, 2) COMMENT '场馆面积',
    facilities VARCHAR(500) COMMENT '场馆设施描述',
    images TEXT COMMENT '场馆图片URL列表',
    tags TEXT COMMENT '场馆标签',
    rating DECIMAL(3, 2) DEFAULT 0.00 COMMENT '评分',
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆表';

-- 创建场馆价格表
CREATE TABLE IF NOT EXISTS venue_prices (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    price_type VARCHAR(20) NOT NULL COMMENT '价格类型：HOURLY(按小时), DAILY(按天), PACKAGE(套餐)',
    price DECIMAL(10, 2) NOT NULL COMMENT '价格',
    currency VARCHAR(3) DEFAULT 'CNY' COMMENT '货币单位',
    description VARCHAR(200) COMMENT '价格描述',
    is_active BOOLEAN DEFAULT TRUE COMMENT '是否有效',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    INDEX idx_venue_id (venue_id),
    INDEX idx_price_type (price_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆价格表';

-- 创建场馆预约表
CREATE TABLE IF NOT EXISTS venue_reservations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    reservation_date DATE NOT NULL COMMENT '预约日期',
    start_time TIME NOT NULL COMMENT '开始时间',
    end_time TIME NOT NULL COMMENT '结束时间',
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '预约状态：PENDING(待确认), CONFIRMED(已确认), CANCELLED(已取消), COMPLETED(已完成)',
    total_amount DECIMAL(10, 2) COMMENT '总金额',
    payment_status VARCHAR(20) DEFAULT 'UNPAID' COMMENT '支付状态：UNPAID(未支付), PAID(已支付), REFUNDED(已退款)',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    INDEX idx_venue_id (venue_id),
    INDEX idx_user_id (user_id),
    INDEX idx_reservation_date (reservation_date),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆预约表';

-- 创建场馆打卡表
CREATE TABLE IF NOT EXISTS venue_check_ins (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    venue_id BIGINT NOT NULL COMMENT '场馆ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    check_in_time DATETIME NOT NULL COMMENT '打卡时间',
    check_out_time DATETIME COMMENT '签出时间',
    duration_minutes INT COMMENT '使用时长(分钟)',
    points_earned INT DEFAULT 0 COMMENT '获得积分',
    notes TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    FOREIGN KEY (venue_id) REFERENCES venues(id) ON DELETE CASCADE,
    INDEX idx_venue_id (venue_id),
    INDEX idx_user_id (user_id),
    INDEX idx_check_in_time (check_in_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆打卡表';

-- 插入测试数据
INSERT INTO venues (name, description, type, sub_type, merchant_id, merchant_name, address, longitude, latitude, phone, open_time, close_time, capacity, area, facilities, rating, rating_count) VALUES
('星光篮球馆', '专业室内篮球场，配备标准篮球架和木地板', 'INDOOR', 'BASKETBALL', 1, '星光体育', '北京市朝阳区建国路88号', 116.4074, 39.9042, '010-12345678', '09:00', '22:00', 50, 800.00, '木地板,标准篮球架,更衣室,淋浴', 4.5, 120),
('绿茵足球场', '11人制标准足球场，天然草坪', 'OUTDOOR', 'FOOTBALL', 1, '星光体育', '北京市海淀区中关村大街1号', 116.3074, 39.9842, '010-87654321', '08:00', '21:00', 22, 7000.00, '天然草坪,标准球门,照明设备', 4.8, 85),
('网球中心', '专业网球场地，硬地材质', 'INDOOR', 'TENNIS', 2, '网球天地', '上海市浦东新区陆家嘴路100号', 121.5074, 31.2042, '021-12345678', '07:00', '23:00', 8, 1200.00, '硬地材质,标准网球场,休息区', 4.6, 95); 