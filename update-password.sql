USE sport_venue;

UPDATE users 
SET password = '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi'
WHERE username = 'admin';

SELECT id, username, password, user_type, status 
FROM users 
WHERE username = 'admin'; 