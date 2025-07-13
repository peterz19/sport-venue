-- 数据库迁移脚本：更新venues表结构
-- 执行前请备份数据库

USE sport_venue;

-- 1. 添加新字段
ALTER TABLE venues 
ADD COLUMN space_type VARCHAR(20) NOT NULL DEFAULT 'INDOOR' COMMENT '空间类型：INDOOR(室内), OUTDOOR(室外)' AFTER type,
ADD COLUMN charge_type VARCHAR(20) NOT NULL DEFAULT 'FREE' COMMENT '付费类型：PAID(付费), FREE(免费)' AFTER space_type;

-- 2. 更新现有数据的空间类型和付费类型
-- 根据原有的sub_type字段推断新的类型
UPDATE venues SET 
    space_type = CASE 
        WHEN sub_type IN ('BASKETBALL', 'TENNIS', 'SWIMMING', 'GYM') THEN 'INDOOR'
        WHEN sub_type IN ('FOOTBALL', 'BASEBALL', 'SOCCER') THEN 'OUTDOOR'
        ELSE 'INDOOR'
    END,
    charge_type = CASE 
        WHEN sub_type IN ('GYM', 'SWIMMING') THEN 'PAID'
        ELSE 'FREE'
    END;

-- 3. 删除旧的sub_type字段
ALTER TABLE venues DROP COLUMN sub_type;

-- 4. 添加新字段的索引
ALTER TABLE venues 
ADD INDEX idx_space_type (space_type),
ADD INDEX idx_charge_type (charge_type);

-- 5. 更新测试数据
UPDATE venues SET 
    type = 'GYM',
    space_type = 'INDOOR',
    charge_type = 'PAID'
WHERE name = '星光篮球馆';

UPDATE venues SET 
    type = 'STADIUM',
    space_type = 'OUTDOOR',
    charge_type = 'FREE'
WHERE name = '绿茵足球场';

UPDATE venues SET 
    type = 'GYM',
    space_type = 'INDOOR',
    charge_type = 'PAID'
WHERE name = '网球中心';

-- 6. 修复商户表create_time字段默认值
ALTER TABLE merchants 
MODIFY COLUMN create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
MODIFY COLUMN update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 7. 插入商户测试数据（如果表为空）
INSERT IGNORE INTO merchants (merchant_code, name, short_name, merchant_type, status, contact_name, contact_phone, contact_email, address, description, rating, rating_count) VALUES
('M001', '星光体育', '星光', 'COMPANY', 'ACTIVE', '张三', '13800138001', 'zhangsan@xingguang.com', '北京市朝阳区建国路88号', '专业体育场馆运营商，提供篮球、足球、网球等多种运动场地', 4.5, 120),
('M002', '网球天地', '网球', 'COMPANY', 'ACTIVE', '李四', '13800138002', 'lisi@tennis.com', '上海市浦东新区陆家嘴路100号', '专业网球场地运营商，提供室内外网球场地', 4.6, 95),
('M003', '健身中心', '健身', 'COMPANY', 'ACTIVE', '王五', '13800138003', 'wangwu@fitness.com', '广州市天河区珠江路200号', '综合性健身中心，提供器械健身、瑜伽、游泳等服务', 4.3, 80); 