-- 测试场馆中文数据
USE sport_venue;

-- 重新插入测试数据
DELETE FROM venues WHERE id = 1;
INSERT INTO venues (id, name, description, address, type, space_type, charge_type, status, capacity, current_occupancy, area, latitude, longitude, phone, open_time, close_time, facilities, rating, rating_count, reservation_enabled, check_in_enabled, points_enabled, create_time, update_time) VALUES 
(1, '专业室内篮球场', '专业室内篮球场，配备标准篮球架和木地板', '北京市朝阳区建国路88号', 'GYM', 'INDOOR', 'PER_HOUR', 'ACTIVE', 50, 0, 800.00, 39.90, 116.41, '010-12345678', '09:00', '22:00', '木地板,篮球架,更衣室,淋浴', 4.50, 120, true, true, true, NOW(), NOW());

-- 验证数据
SELECT id, name, description, address FROM venues WHERE id = 1; 