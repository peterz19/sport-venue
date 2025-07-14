-- 检查场馆枚举问题的详细SQL脚本
USE sport_venue;

-- 1. 检查所有场馆数据
SELECT '=== 所有场馆数据 ===' as info;
SELECT id, name, type, space_type, charge_type, status FROM venues ORDER BY id;

-- 2. 检查type字段的所有值
SELECT '=== type字段的所有值 ===' as info;
SELECT type, COUNT(*) as count FROM venues GROUP BY type ORDER BY type;

-- 3. 检查space_type字段的所有值
SELECT '=== space_type字段的所有值 ===' as info;
SELECT space_type, COUNT(*) as count FROM venues GROUP BY space_type ORDER BY space_type;

-- 4. 检查charge_type字段的所有值
SELECT '=== charge_type字段的所有值 ===' as info;
SELECT charge_type, COUNT(*) as count FROM venues GROUP BY charge_type ORDER BY charge_type;

-- 5. 检查status字段的所有值
SELECT '=== status字段的所有值 ===' as info;
SELECT status, COUNT(*) as count FROM venues GROUP BY status ORDER BY status;

-- 6. 检查是否有空值或空字符串
SELECT '=== 检查空值 ===' as info;
SELECT 
    COUNT(*) as total_venues,
    SUM(CASE WHEN type IS NULL OR type = '' THEN 1 ELSE 0 END) as null_type_count,
    SUM(CASE WHEN space_type IS NULL OR space_type = '' THEN 1 ELSE 0 END) as null_space_type_count,
    SUM(CASE WHEN charge_type IS NULL OR charge_type = '' THEN 1 ELSE 0 END) as null_charge_type_count,
    SUM(CASE WHEN status IS NULL OR status = '' THEN 1 ELSE 0 END) as null_status_count
FROM venues;

-- 7. 检查type字段的非法值
SELECT '=== type字段非法值检查 ===' as info;
SELECT id, name, type, space_type, charge_type 
FROM venues 
WHERE type NOT IN ('PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER')
   OR type IS NULL 
   OR type = '';

-- 8. 检查space_type字段的非法值
SELECT '=== space_type字段非法值检查 ===' as info;
SELECT id, name, type, space_type, charge_type 
FROM venues 
WHERE space_type NOT IN ('INDOOR', 'OUTDOOR')
   OR space_type IS NULL 
   OR space_type = '';

-- 9. 检查charge_type字段的非法值
SELECT '=== charge_type字段非法值检查 ===' as info;
SELECT id, name, type, space_type, charge_type 
FROM venues 
WHERE charge_type NOT IN ('PAID', 'FREE')
   OR charge_type IS NULL 
   OR charge_type = '';

-- 10. 检查status字段的非法值
SELECT '=== status字段非法值检查 ===' as info;
SELECT id, name, type, space_type, charge_type, status 
FROM venues 
WHERE status NOT IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')
   OR status IS NULL 
   OR status = '';

-- 11. 检查是否有字段值混淆的情况
SELECT '=== 检查字段值混淆 ===' as info;
SELECT id, name, type, space_type, charge_type 
FROM venues 
WHERE type IN ('INDOOR', 'OUTDOOR') 
   OR space_type IN ('PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER')
   OR charge_type IN ('INDOOR', 'OUTDOOR', 'PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER'); 