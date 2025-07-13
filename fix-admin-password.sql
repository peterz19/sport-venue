-- 修复admin用户密码的SQL脚本
USE sport_venue;

-- 1. 删除现有的admin用户及其角色关联
DELETE FROM user_roles WHERE user_id IN (SELECT id FROM users WHERE username='admin');
DELETE FROM users WHERE username='admin';

-- 2. 重新插入admin用户（使用新的BCrypt密码）
INSERT INTO users (username, password, real_name, user_type, status, create_by) VALUES
('admin', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', '系统管理员', 'ADMIN', 'ACTIVE', 1);

-- 3. 重新分配ADMIN角色
INSERT INTO user_roles (user_id, role_id, create_by)
SELECT u.id, r.id, 1
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN';

-- 4. 验证插入结果
SELECT '用户表:' as info;
SELECT id, username, real_name, user_type, status FROM users WHERE username='admin';

SELECT '用户角色关联:' as info;
SELECT ur.id, u.username, r.role_name 
FROM user_roles ur 
JOIN users u ON ur.user_id = u.id 
JOIN roles r ON ur.role_id = r.id
WHERE u.username = 'admin'; 