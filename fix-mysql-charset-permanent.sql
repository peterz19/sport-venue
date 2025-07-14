-- 永久修复MySQL字符集配置
-- 设置全局字符集为utf8mb4

-- 设置全局字符集变量
SET GLOBAL character_set_client = utf8mb4;
SET GLOBAL character_set_connection = utf8mb4;
SET GLOBAL character_set_results = utf8mb4;

-- 设置会话字符集
SET SESSION character_set_client = utf8mb4;
SET SESSION character_set_connection = utf8mb4;
SET SESSION character_set_results = utf8mb4;

-- 设置数据库默认字符集
ALTER DATABASE sport_venue CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 检查当前字符集设置
SELECT '=== 当前字符集设置 ===' as info;
SHOW VARIABLES LIKE 'character_set%';

-- 检查数据库字符集
SELECT '=== 数据库字符集 ===' as info;
SELECT SCHEMA_NAME, DEFAULT_CHARACTER_SET_NAME, DEFAULT_COLLATION_NAME 
FROM INFORMATION_SCHEMA.SCHEMATA 
WHERE SCHEMA_NAME = 'sport_venue'; 