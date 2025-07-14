-- 详细检查场馆数据
USE sport_venue;

-- 1. 检查所有场馆数据
SELECT '=== 所有场馆数据 ===' as info;
SELECT id, name, type, space_type, charge_type, status FROM venues ORDER BY id;

-- 2. 检查type字段的所有不同值
SELECT '=== type字段的所有不同值 ===' as info;
SELECT DISTINCT type, COUNT(*) as count FROM venues GROUP BY type;

-- 3. 检查space_type字段的所有不同值
SELECT '=== space_type字段的所有不同值 ===' as info;
SELECT DISTINCT space_type, COUNT(*) as count FROM venues GROUP BY space_type;

-- 4. 检查charge_type字段的所有不同值
SELECT '=== charge_type字段的所有不同值 ===' as info;
SELECT DISTINCT charge_type, COUNT(*) as count FROM venues GROUP BY charge_type;

-- 5. 检查status字段的所有不同值
SELECT '=== status字段的所有不同值 ===' as info;
SELECT DISTINCT status, COUNT(*) as count FROM venues GROUP BY status;

-- 6. 检查是否有空值
SELECT '=== 检查空值 ===' as info;
SELECT 
    COUNT(*) as total_venues,
    SUM(CASE WHEN type IS NULL OR type = '' THEN 1 ELSE 0 END) as null_type_count,
    SUM(CASE WHEN space_type IS NULL OR space_type = '' THEN 1 ELSE 0 END) as null_space_type_count,
    SUM(CASE WHEN charge_type IS NULL OR charge_type = '' THEN 1 ELSE 0 END) as null_charge_type_count,
    SUM(CASE WHEN status IS NULL OR status = '' THEN 1 ELSE 0 END) as null_status_count
FROM venues;

-- 7. 检查是否有非法值（不在枚举中的值）
SELECT '=== 检查非法值 ===' as info;
SELECT 'type字段非法值:' as field, type as value, COUNT(*) as count 
FROM venues 
WHERE type NOT IN ('PARK', 'INSTITUTION', 'STADIUM', 'GYM', 'OTHER')
GROUP BY type
UNION ALL
SELECT 'space_type字段非法值:' as field, space_type as value, COUNT(*) as count 
FROM venues 
WHERE space_type NOT IN ('INDOOR', 'OUTDOOR')
GROUP BY space_type
UNION ALL
SELECT 'charge_type字段非法值:' as field, charge_type as value, COUNT(*) as count 
FROM venues 
WHERE charge_type NOT IN ('PAID', 'FREE')
GROUP BY charge_type
UNION ALL
SELECT 'status字段非法值:' as field, status as value, COUNT(*) as count 
FROM venues 
WHERE status NOT IN ('ACTIVE', 'INACTIVE', 'MAINTENANCE')
GROUP BY status;

-- 8. 检查表结构
SELECT '=== 表结构 ===' as info;
DESCRIBE venues; 