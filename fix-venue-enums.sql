-- 修正场馆枚举值SQL脚本
-- 执行前请备份数据库

USE sport_venue;

-- 1. 检查当前枚举值
SELECT '当前type字段值:' as info;
SELECT DISTINCT type FROM venues;

SELECT '当前space_type字段值:' as info;
SELECT DISTINCT space_type FROM venues;

SELECT '当前charge_type字段值:' as info;
SELECT DISTINCT charge_type FROM venues;

SELECT '当前status字段值:' as info;
SELECT DISTINCT status FROM venues;

-- 2. 修正type字段（场馆类型）
-- 确保所有type值都是有效的枚举值：PARK, INSTITUTION, STADIUM, GYM, OTHER
UPDATE venues SET type = 'GYM' WHERE type NOT IN ('PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER');

-- 3. 修正space_type字段（空间类型）
-- 确保所有space_type值都是有效的枚举值：INDOOR, OUTDOOR
UPDATE venues SET space_type = 'INDOOR' WHERE space_type NOT IN ('INDOOR', 'OUTDOOR');

-- 4. 修正charge_type字段（收费类型）
-- 确保所有charge_type值都是有效的枚举值：PAID, FREE
UPDATE venues SET charge_type = 'PAID' WHERE charge_type NOT IN ('PAID', 'FREE');

-- 5. 修正status字段（场馆状态）
-- 确保所有status值都是有效的枚举值：ACTIVE, INACTIVE, MAINTENANCE
UPDATE venues SET status = 'ACTIVE' WHERE status NOT IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE');

-- 6. 验证修正结果
SELECT '修正后的type字段值:' as info;
SELECT DISTINCT type FROM venues;

SELECT '修正后的space_type字段值:' as info;
SELECT DISTINCT space_type FROM venues;

SELECT '修正后的charge_type字段值:' as info;
SELECT DISTINCT charge_type FROM venues;

SELECT '修正后的status字段值:' as info;
SELECT DISTINCT status FROM venues;

-- 7. 显示所有场馆信息
SELECT '所有场馆信息:' as info;
SELECT id, name, type, space_type, charge_type, status FROM venues ORDER BY id;

-- 8. 检查是否有空值
SELECT '检查空值:' as info;
SELECT COUNT(*) as total_venues FROM venues;
SELECT COUNT(*) as null_type_count FROM venues WHERE type IS NULL;
SELECT COUNT(*) as null_space_type_count FROM venues WHERE space_type IS NULL;
SELECT COUNT(*) as null_charge_type_count FROM venues WHERE charge_type IS NULL;
SELECT COUNT(*) as null_status_count FROM venues WHERE status IS NULL; 