-- 修复admin用户密码
USE sport_venue;

-- 更新admin用户的密码为正确的BCrypt加密结果
UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE username = 'admin';

-- 验证更新结果
SELECT id, username, password, user_type, status 
FROM users 
WHERE username = 'admin'; 