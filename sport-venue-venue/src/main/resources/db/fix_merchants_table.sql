-- 修复商户表create_time字段默认值
-- 执行前请备份数据库

USE sport_venue;

-- 检查merchants表是否存在
SELECT COUNT(*) FROM information_schema.tables 
WHERE table_schema = 'sport_venue' AND table_name = 'merchants';

-- 如果表不存在，先创建表
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

-- 如果表已存在但create_time字段没有默认值，修改字段
ALTER TABLE merchants 
MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 插入商户测试数据（如果表为空）
INSERT IGNORE INTO merchants (merchant_code, name, short_name, merchant_type, status, contact_name, contact_phone, contact_email, address, description, rating, rating_count) VALUES
('M001', '星光体育', '星光', 'COMPANY', 'ACTIVE', '张三', '13800138001', 'zhangsan@xingguang.com', '北京市朝阳区建国路88号', '专业体育场馆运营商，提供篮球、足球、网球等多种运动场地', 4.5, 120),
('M002', '网球天地', '网球', 'COMPANY', 'ACTIVE', '李四', '13800138002', 'lisi@tennis.com', '上海市浦东新区陆家嘴路100号', '专业网球场地运营商，提供室内外网球场地', 4.6, 95),
('M003', '健身中心', '健身', 'COMPANY', 'ACTIVE', '王五', '13800138003', 'wangwu@fitness.com', '广州市天河区珠江路200号', '综合性健身中心，提供器械健身、瑜伽、游泳等服务', 4.3, 80);

-- 验证数据插入成功
SELECT * FROM merchants; 