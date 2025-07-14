-- 修复场馆中文数据
USE sport_venue;

-- 更新场馆数据为正确的中文
UPDATE venues SET 
    name = '专业室内篮球场',
    description = '专业室内篮球场，配备标准篮球架和木地板',
    address = '北京市朝阳区建国路88号'
WHERE id = 1;

UPDATE venues SET 
    name = '绿茵足球场',
    description = '11人制标准足球场，天然草坪',
    address = '北京市海淀区中关村大街1号'
WHERE id = 2;

UPDATE venues SET 
    name = '网球中心',
    description = '专业网球场地，硬地材质',
    address = '上海市浦东新区陆家嘴路100号'
WHERE id = 3;

UPDATE venues SET 
    name = '社区公园',
    description = '社区公共运动公园，免费开放',
    address = '北京市西城区西单大街50号'
WHERE id = 4;

UPDATE venues SET 
    name = '游泳馆',
    description = '标准50米泳池，恒温控制',
    address = '广州市天河区珠江路200号'
WHERE id = 5;

-- 验证更新结果
SELECT id, name, description, address FROM venues ORDER BY id; 