-- 测试商户账号（密码：123456）
-- 依赖：merchants 表至少有 id=1 的商户

INSERT INTO users (username, password, real_name, phone, user_type, merchant_id, merchant_name, status, points, create_time, update_time)
SELECT 'merchant001',
       '$2a$10$m2ZagLbGG5qZ4ClswCunvO9AASz5p0YRiEy74VYsL2yndpJpTDIEi',
       '测试商户',
       '13800000001',
       'B_MERCHANT',
       1,
       '测试商户1',
       'ACTIVE',
       0,
       NOW(),
       NOW()
FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'merchant001');
