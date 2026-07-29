-- SaaS 模块：审计 / 功能开关 / 微信渠道 / C 端用户与钱包
-- 手工执行；本地亦依赖 JPA ddl-auto=update 自动建表

USE sport_venue;

CREATE TABLE IF NOT EXISTS platform_audit_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    action VARCHAR(40) NOT NULL,
    target_type VARCHAR(20) NOT NULL,
    target_id BIGINT,
    merchant_id BIGINT,
    before_json TEXT,
    after_json TEXT,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pal_merchant (merchant_id),
    INDEX idx_pal_action (action)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台操作审计';

CREATE TABLE IF NOT EXISTS merchant_features (
    merchant_id BIGINT PRIMARY KEY,
    enable_cashier TINYINT NOT NULL DEFAULT 1,
    enable_booking TINYINT NOT NULL DEFAULT 1,
    enable_team_match TINYINT NOT NULL DEFAULT 1,
    enable_c_end TINYINT NOT NULL DEFAULT 0,
    enable_recharge TINYINT NOT NULL DEFAULT 0,
    max_staff INT NOT NULL DEFAULT 50,
    max_venues INT NOT NULL DEFAULT 20,
    max_courts INT NOT NULL DEFAULT 100,
    max_wx_mini INT NOT NULL DEFAULT 1,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户功能开关与配额';

CREATE TABLE IF NOT EXISTS merchant_wx_channels (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    channel_type VARCHAR(20) NOT NULL COMMENT 'MINI_PROGRAM/OFFICIAL_ACCOUNT',
    app_id VARCHAR(64) NOT NULL,
    app_secret_enc VARCHAR(512),
    oa_server_token VARCHAR(64),
    oa_encoding_aes_key VARCHAR(64),
    bind_status VARCHAR(20) NOT NULL DEFAULT 'UNSET',
    auth_type VARCHAR(20) NOT NULL DEFAULT 'SELF',
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by BIGINT,
    UNIQUE KEY uk_mwc_app (app_id),
    UNIQUE KEY uk_mwc_merchant_type (merchant_id, channel_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户微信渠道';

CREATE TABLE IF NOT EXISTS merchant_wx_pay (
    merchant_id BIGINT PRIMARY KEY,
    mch_id VARCHAR(64),
    mch_api_v3_key_enc VARCHAR(512),
    mch_serial_no VARCHAR(128),
    notify_path VARCHAR(100),
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户微信支付';

CREATE TABLE IF NOT EXISTS customer_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    nickname VARCHAR(50),
    phone VARCHAR(20),
    avatar VARCHAR(200),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_cu_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端顾客';

CREATE TABLE IF NOT EXISTS user_wx_identities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    channel_type VARCHAR(20) NOT NULL,
    app_id VARCHAR(64) NOT NULL,
    openid VARCHAR(64) NOT NULL,
    unionid VARCHAR(64),
    customer_user_id BIGINT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_uwi_app_openid (app_id, openid),
    INDEX idx_uwi_customer (customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信身份绑定';

CREATE TABLE IF NOT EXISTS customer_wallets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    balance DECIMAL(12,2) NOT NULL DEFAULT 0,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_cw_merchant_customer (merchant_id, customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='C端钱包';

CREATE TABLE IF NOT EXISTS wallet_ledgers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    change_amount DECIMAL(12,2) NOT NULL,
    balance_after DECIMAL(12,2) NOT NULL,
    biz_type VARCHAR(30) NOT NULL COMMENT 'RECHARGE/BOOKING_PAY/BOOKING_REFUND',
    biz_id BIGINT,
    remark VARCHAR(200),
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_wl_customer (customer_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='钱包流水';

CREATE TABLE IF NOT EXISTS wallet_recharge_orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no VARCHAR(32) NOT NULL UNIQUE,
    merchant_id BIGINT NOT NULL,
    customer_user_id BIGINT NOT NULL,
    amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    pay_channel VARCHAR(20),
    paid_at DATETIME,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_wro_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值单';

-- 可选展示列（若不存在则忽略失败时可手工 ALTER）
-- ALTER TABLE merchants ADD COLUMN wx_mini_bound TINYINT DEFAULT 0;
-- ALTER TABLE merchants ADD COLUMN wx_oa_bound TINYINT DEFAULT 0;
-- ALTER TABLE merchants ADD COLUMN disabled_at DATETIME NULL;
