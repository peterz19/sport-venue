-- 修复admin用户权限的完整SQL脚本
USE sport_venue;

-- 1. 检查当前admin用户状态
SELECT '=== 当前admin用户状态 ===' as info;
SELECT id, username, real_name, user_type, status FROM users WHERE username='admin';

-- 2. 检查当前角色分配
SELECT '=== 当前角色分配 ===' as info;
SELECT ur.id, u.username, r.role_name, r.role_code
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
JOIN roles r ON ur.role_id = r.id
WHERE u.username='admin';

-- 3. 检查当前权限分配
SELECT '=== 当前权限分配 ===' as info;
SELECT rp.id, r.role_name, p.permission_name, p.permission_code
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.role_code = 'ADMIN'
ORDER BY p.permission_code;

-- 4. 确保admin用户分配了ADMIN角色
INSERT IGNORE INTO user_roles (user_id, role_id, create_by)
SELECT u.id, r.id, 1
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

-- 5. 为ADMIN角色分配所有权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.role_code = 'ADMIN';

-- 6. 验证修复结果
SELECT '=== 修复后的角色分配 ===' as info;
SELECT ur.id, u.username, r.role_name, r.role_code
FROM user_roles ur
JOIN users u ON ur.user_id = u.id
JOIN roles r ON ur.role_id = r.id
WHERE u.username='admin';

SELECT '=== 修复后的权限分配 ===' as info;
SELECT rp.id, r.role_name, p.permission_name, p.permission_code
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
JOIN permissions p ON rp.permission_id = p.id
WHERE r.role_code = 'ADMIN'
ORDER BY p.permission_code;

SELECT '=== 权限统计 ===' as info;
SELECT
    'ADMIN角色权限总数' as description,
    COUNT(*) as count
FROM role_permissions rp
JOIN roles r ON rp.role_id = r.id
WHERE r.role_code = 'ADMIN'
UNION ALL
SELECT
    '系统总权限数' as description,
    COUNT(*) as count
FROM permissions;

SELECT '=== 修复完成 ===' as info;



