-- 修复本地MySQL数据库中场馆枚举值
USE sport_venue;

-- 检查当前数据
SELECT '=== 当前场馆数据 ===' as info;
SELECT id, name, type, space_type, charge_type FROM venues;

-- 首先修改type字段的枚举定义
ALTER TABLE venues MODIFY COLUMN type ENUM('PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER') NOT NULL;

-- 修复type字段：将INDOOR/OUTDOOR改为正确的VenueType枚举值
UPDATE venues SET type = 'GYM' WHERE type = 'INDOOR';
UPDATE venues SET type = 'PARK' WHERE type = 'OUTDOOR';

-- 修复space_type字段：确保有正确的值
UPDATE venues SET space_type = 'INDOOR' WHERE space_type IS NULL OR space_type = '';
UPDATE venues SET space_type = 'OUTDOOR' WHERE id IN (2, 4); -- 足球场和公园设为室外

-- 修复charge_type字段：确保有正确的值
UPDATE venues SET charge_type = 'PAID' WHERE charge_type IS NULL OR charge_type = '';

-- 验证修复结果
SELECT '=== 修复后的场馆数据 ===' as info;
SELECT id, name, type, space_type, charge_type FROM venues; 