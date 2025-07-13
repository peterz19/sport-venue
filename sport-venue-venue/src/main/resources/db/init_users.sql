-- 创建用户表
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码（加密存储）',
    real_name VARCHAR(50) COMMENT '真实姓名',
    phone VARCHAR(20) UNIQUE COMMENT '手机号',
    email VARCHAR(100) UNIQUE COMMENT '邮箱',
    user_type VARCHAR(20) NOT NULL COMMENT '用户类型：B_MERCHANT(B端商户), B_STAFF(B端员工), C_USER(C端用户), ADMIN(系统管理员)',
    merchant_id BIGINT COMMENT '所属商户ID（B端用户）',
    merchant_name VARCHAR(100) COMMENT '商户名称（冗余字段）',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '用户状态：ACTIVE(正常), INACTIVE(停用), LOCKED(锁定)',
    avatar VARCHAR(200) COMMENT '头像URL',
    points INT DEFAULT 0 COMMENT '积分余额',
    member_level VARCHAR(20) DEFAULT 'BRONZE' COMMENT '会员等级：BRONZE(青铜), SILVER(白银), GOLD(黄金), PLATINUM(铂金), DIAMOND(钻石)',
    last_login_time DATETIME COMMENT '最后登录时间',
    last_login_ip VARCHAR(50) COMMENT '最后登录IP',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_username (username),
    INDEX idx_phone (phone),
    INDEX idx_email (email),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_user_type (user_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 创建角色表
CREATE TABLE IF NOT EXISTS roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_code VARCHAR(50) NOT NULL UNIQUE COMMENT '角色编码',
    role_name VARCHAR(100) NOT NULL COMMENT '角色名称',
    description VARCHAR(200) COMMENT '角色描述',
    role_type VARCHAR(20) NOT NULL COMMENT '角色类型：SYSTEM(系统角色), CUSTOM(自定义角色)',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '角色状态：ACTIVE(正常), INACTIVE(停用)',
    sort INT DEFAULT 0 COMMENT '排序',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_role_code (role_code),
    INDEX idx_role_type (role_type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 创建权限表
CREATE TABLE IF NOT EXISTS permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    permission_code VARCHAR(100) NOT NULL UNIQUE COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(200) COMMENT '权限描述',
    permission_type VARCHAR(20) NOT NULL COMMENT '权限类型：MENU(菜单), BUTTON(按钮), API(接口)',
    category VARCHAR(20) NOT NULL COMMENT '权限分类：VENUE(场馆), RESERVATION(预约), CHECKIN(打卡), USER(用户), SYSTEM(系统)',
    resource_path VARCHAR(200) COMMENT '资源路径（API路径或菜单路径）',
    http_method VARCHAR(10) COMMENT 'HTTP方法（GET, POST, PUT, DELETE等）',
    parent_id BIGINT COMMENT '父权限ID',
    sort INT DEFAULT 0 COMMENT '排序',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '权限状态：ACTIVE(正常), INACTIVE(停用)',
    remark VARCHAR(500) COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    create_by BIGINT COMMENT '创建人ID',
    update_by BIGINT COMMENT '更新人ID',
    INDEX idx_permission_code (permission_code),
    INDEX idx_permission_type (permission_type),
    INDEX idx_category (category),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表';

-- 创建用户角色关联表
CREATE TABLE IF NOT EXISTS user_roles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_user_role (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- 创建角色权限关联表
CREATE TABLE IF NOT EXISTS role_permissions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    create_by BIGINT COMMENT '创建人ID',
    UNIQUE KEY uk_role_permission (role_id, permission_id),
    FOREIGN KEY (role_id) REFERENCES roles(id) ON DELETE CASCADE,
    FOREIGN KEY (permission_id) REFERENCES permissions(id) ON DELETE CASCADE,
    INDEX idx_role_id (role_id),
    INDEX idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色权限关联表';

-- 插入基础角色数据
INSERT IGNORE INTO roles (role_code, role_name, description, role_type, status, sort) VALUES
('ADMIN', '系统管理员', '系统管理员，拥有所有权限', 'SYSTEM', 'ACTIVE', 1),
('MERCHANT', '商户管理员', '商户管理员，管理自己的场馆', 'SYSTEM', 'ACTIVE', 2),
('STAFF', '员工', '商户员工，协助管理场馆', 'SYSTEM', 'ACTIVE', 3),
('USER', '普通用户', 'C端用户，可以预约场馆', 'SYSTEM', 'ACTIVE', 4);

-- 插入基础权限数据
INSERT IGNORE INTO permissions (permission_code, permission_name, description, permission_type, category, resource_path, http_method, sort) VALUES
-- 系统管理权限
('system:dashboard:view', '查看数据看板', '查看系统数据看板', 'MENU', 'SYSTEM', '/dashboard', 'GET', 1),
('system:user:view', '查看用户管理', '查看用户管理页面', 'MENU', 'USER', '/user/list', 'GET', 2),
('system:user:add', '添加用户', '添加新用户', 'BUTTON', 'USER', '/user/add', 'POST', 3),
('system:user:edit', '编辑用户', '编辑用户信息', 'BUTTON', 'USER', '/user/edit', 'PUT', 4),
('system:user:delete', '删除用户', '删除用户', 'BUTTON', 'USER', '/user/delete', 'DELETE', 5),

-- 场馆管理权限
('venue:list:view', '查看场馆列表', '查看场馆列表页面', 'MENU', 'VENUE', '/venue/list', 'GET', 6),
('venue:add', '添加场馆', '添加新场馆', 'BUTTON', 'VENUE', '/venue/add', 'POST', 7),
('venue:edit', '编辑场馆', '编辑场馆信息', 'BUTTON', 'VENUE', '/venue/edit', 'PUT', 8),
('venue:delete', '删除场馆', '删除场馆', 'BUTTON', 'VENUE', '/venue/delete', 'DELETE', 9),
('venue:detail:view', '查看场馆详情', '查看场馆详细信息', 'BUTTON', 'VENUE', '/venue/detail', 'GET', 10),

-- API权限
('api:auth:login', '用户登录', '用户登录接口', 'API', 'SYSTEM', '/auth/login', 'POST', 11),
('api:auth:logout', '用户登出', '用户登出接口', 'API', 'SYSTEM', '/auth/logout', 'POST', 12),
('api:auth:user:info', '获取用户信息', '获取当前用户信息接口', 'API', 'SYSTEM', '/auth/user/info', 'GET', 13),
('api:venue:list', '获取场馆列表', '获取场馆列表接口', 'API', 'VENUE', '/venues', 'GET', 14),
('api:venue:detail', '获取场馆详情', '获取场馆详情接口', 'API', 'VENUE', '/venues/{id}', 'GET', 15),
('api:venue:create', '创建场馆', '创建场馆接口', 'API', 'VENUE', '/venues', 'POST', 16),
('api:venue:update', '更新场馆', '更新场馆接口', 'API', 'VENUE', '/venues/{id}', 'PUT', 17),
('api:venue:delete', '删除场馆', '删除场馆接口', 'API', 'VENUE', '/venues/{id}', 'DELETE', 18);

-- 为管理员角色分配所有权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.role_code = 'ADMIN';

-- 为商户管理员角色分配场馆管理权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.role_code = 'MERCHANT' 
AND p.category IN ('VENUE', 'SYSTEM')
AND p.permission_code NOT LIKE 'system:user:%';

-- 为员工角色分配场馆查看权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.role_code = 'STAFF' 
AND p.category = 'VENUE'
AND p.permission_code LIKE '%:view';

-- 为普通用户角色分配场馆查看权限
INSERT IGNORE INTO role_permissions (role_id, permission_id, create_by)
SELECT r.id, p.id, 1
FROM roles r, permissions p
WHERE r.role_code = 'USER' 
AND p.category = 'VENUE'
AND p.permission_code LIKE '%:view';

-- 插入管理员用户（密码：123456，使用BCrypt加密）
INSERT IGNORE INTO users (username, password, real_name, user_type, status, create_by) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVEFDa', '系统管理员', 'ADMIN', 'ACTIVE', 1);

-- 为管理员用户分配管理员角色
INSERT IGNORE INTO user_roles (user_id, role_id, create_by)
SELECT u.id, r.id, 1
FROM users u, roles r
WHERE u.username = 'admin' AND r.role_code = 'ADMIN'; 