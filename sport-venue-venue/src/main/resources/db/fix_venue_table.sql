-- 修复场馆表结构，使其与实体类匹配
USE sport_venue;

-- 0. 创建商户表（如果不存在）
CREATE TABLE IF NOT EXISTS merchants (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_code VARCHAR(50) NOT NULL UNIQUE COMMENT '商户编码',
    name VARCHAR(100) NOT NULL COMMENT '商户名称',
    short_name VARCHAR(50) COMMENT '商户简称',
    merchant_type VARCHAR(20) NOT NULL COMMENT '商户类型：INDIVIDUAL(个人), COMPANY(企业), CHAIN(连锁)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '商户状态：ACTIVE(正常), INACTIVE(停用), SUSPENDED(暂停)',
    contact_name VARCHAR(50) COMMENT '联系人姓名',
    contact_phone VARCHAR(20) COMMENT '联系人手机号',
    contact_email VARCHAR(100) COMMENT '联系人邮箱',
    address VARCHAR(200) COMMENT '商户地址',
    business_license VARCHAR(50) COMMENT '营业执照号',
    legal_person VARCHAR(50) COMMENT '法人姓名',
    legal_person_id VARCHAR(20) COMMENT '法人身份证号',
    registered_capital DECIMAL(15, 2) COMMENT '注册资本',
    establishment_date DATETIME COMMENT '成立日期',
    business_hours VARCHAR(100) COMMENT '营业时间',
    description VARCHAR(1000) COMMENT '商户简介',
    logo VARCHAR(200) COMMENT '商户logo',
    images VARCHAR(1000) COMMENT '商户图片',
    tags VARCHAR(500) COMMENT '商户标签',
    rating DECIMAL(3, 2) DEFAULT 0.00 COMMENT '评分',
    rating_count INT DEFAULT 0 COMMENT '评分人数',
    venue_count INT DEFAULT 0 COMMENT '场馆数量',
    total_revenue DECIMAL(15, 2) DEFAULT 0.00 COMMENT '总营业额',
    monthly_revenue DECIMAL(15, 2) DEFAULT 0.00 COMMENT '本月营业额',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_merchant_code (merchant_code),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户表';

-- 插入商户测试数据
INSERT INTO merchants (merchant_code, name, short_name, merchant_type, status, contact_name, contact_phone, contact_email, address, description, rating, rating_count, create_time, update_time) VALUES
('M001', '星光体育', '星光', 'COMPANY', 'ACTIVE', '张三', '13800138001', 'zhangsan@xingguang.com', '北京市朝阳区建国路88号', '专业体育场馆运营商，提供篮球、足球、网球等多种运动场地', 4.5, 120, NOW(), NOW()),
('M002', '网球天地', '网球', 'COMPANY', 'ACTIVE', '李四', '13800138002', 'lisi@tennis.com', '上海市浦东新区陆家嘴路100号', '专业网球场地运营商，提供室内外网球场地', 4.6, 95, NOW(), NOW()),
('M003', '健身中心', '健身', 'COMPANY', 'ACTIVE', '王五', '13800138003', 'wangwu@fitness.com', '广州市天河区珠江路200号', '综合性健身中心，提供器械健身、瑜伽、游泳等服务', 4.3, 80, NOW(), NOW())
ON DUPLICATE KEY UPDATE name = VALUES(name);

-- 1. 备份现有数据（如果存在）
CREATE TABLE IF NOT EXISTS venues_backup AS SELECT * FROM venues;

-- 2. 删除旧表
DROP TABLE IF EXISTS venue_prices;
DROP TABLE IF EXISTS venue_reservations;
DROP TABLE IF EXISTS venue_check_ins;
DROP TABLE IF EXISTS venues;

-- 3. 重新创建场馆表（与实体类匹配）
CREATE TABLE venues (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL COMMENT '场馆名称',
    description VARCHAR(500) COMMENT '场馆描述',
    type VARCHAR(20) NOT NULL COMMENT '场馆类型：PARK(公园), INSTITUTION(机构), STADIUM(体育场), GYM(健身房), OTHER(其他)',
    space_type VARCHAR(20) NOT NULL COMMENT '空间类型：INDOOR(室内), OUTDOOR(室外)',
    charge_type VARCHAR(20) NOT NULL COMMENT '付费类型：PAID(付费), FREE(免费)',
    merchant_id BIGINT NOT NULL COMMENT '商户ID',
    merchant_name VARCHAR(100) COMMENT '商户名称',
    address VARCHAR(200) NOT NULL COMMENT '场馆地址',
    longitude DECIMAL(10, 7) COMMENT '经度',
    latitude DECIMAL(10, 7) COMMENT '纬度',
    phone VARCHAR(20) COMMENT '联系电话',
    open_time VARCHAR(10) COMMENT '营业时间开始',
    close_time VARCHAR(10) COMMENT '营业时间结束',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '场馆状态：ACTIVE(正常), INACTIVE(停用), MAINTENANCE(维护中)',
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
    INDEX idx_space_type (space_type),
    INDEX idx_charge_type (charge_type),
    INDEX idx_status (status),
    INDEX idx_location (longitude, latitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='场馆表';

-- 4. 重新创建场馆价格表
CREATE TABLE venue_prices (
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

-- 5. 重新创建场馆预约表
CREATE TABLE venue_reservations (
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

-- 6. 重新创建场馆打卡表
CREATE TABLE venue_check_ins (
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

-- 7. 插入测试数据
INSERT INTO venues (name, description, type, space_type, charge_type, merchant_id, merchant_name, address, longitude, latitude, phone, open_time, close_time, capacity, area, facilities, rating, rating_count) VALUES
('星光篮球馆', '专业室内篮球场，配备标准篮球架和木地板', 'GYM', 'INDOOR', 'PAID', 1, '星光体育', '北京市朝阳区建国路88号', 116.4074, 39.9042, '010-12345678', '09:00', '22:00', 50, 800.00, '木地板,标准篮球架,更衣室,淋浴', 4.5, 120),
('绿茵足球场', '11人制标准足球场，天然草坪', 'STADIUM', 'OUTDOOR', 'FREE', 1, '星光体育', '北京市海淀区中关村大街1号', 116.3074, 39.9842, '010-87654321', '08:00', '21:00', 22, 7000.00, '天然草坪,标准球门,照明设备', 4.8, 85),
('网球中心', '专业网球场地，硬地材质', 'GYM', 'INDOOR', 'PAID', 2, '网球天地', '上海市浦东新区陆家嘴路100号', 121.5074, 31.2042, '021-12345678', '07:00', '23:00', 8, 1200.00, '硬地材质,标准网球场,休息区', 4.6, 95),
('社区公园', '社区公共运动公园，免费开放', 'PARK', 'OUTDOOR', 'FREE', 1, '星光体育', '北京市西城区西单大街50号', 116.3574, 39.9142, '010-98765432', '06:00', '22:00', 100, 5000.00, '健身器材,篮球场,乒乓球台', 4.2, 60),
('游泳馆', '标准50米泳池，恒温控制', 'GYM', 'INDOOR', 'PAID', 3, '健身中心', '广州市天河区珠江路200号', 113.3574, 23.1242, '020-12345678', '06:00', '22:00', 30, 1500.00, '50米泳池,恒温系统,更衣室', 4.7, 110);

-- 8. 验证数据
SELECT '场馆表修复完成，当前数据:' as info;
SELECT id, name, type, space_type, charge_type, status FROM venues; 