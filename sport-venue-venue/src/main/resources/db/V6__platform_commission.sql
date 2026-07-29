-- 平台抽成台账（A1）：规则 / 明细 / 结算单
USE sport_venue;

CREATE TABLE IF NOT EXISTS merchant_commission_rules (
    merchant_id BIGINT PRIMARY KEY,
    rate DECIMAL(8,4) NOT NULL DEFAULT 0 COMMENT '抽成比例，如 0.0300=3%',
    include_cash TINYINT NOT NULL DEFAULT 0,
    include_wechat TINYINT NOT NULL DEFAULT 1,
    include_alipay TINYINT NOT NULL DEFAULT 1,
    enabled TINYINT NOT NULL DEFAULT 1,
    remark VARCHAR(200),
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    update_by BIGINT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商户平台抽成规则';

CREATE TABLE IF NOT EXISTS platform_commission_entries (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id BIGINT NOT NULL,
    biz_type VARCHAR(30) NOT NULL COMMENT 'SALES_ORDER',
    biz_id BIGINT NOT NULL,
    order_no VARCHAR(32),
    pay_method VARCHAR(30) NOT NULL,
    order_amount DECIMAL(12,2) NOT NULL,
    rate DECIMAL(8,4) NOT NULL,
    commission_amount DECIMAL(12,2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/SETTLED',
    settlement_id BIGINT,
    paid_at DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_pce_biz (biz_type, biz_id),
    INDEX idx_pce_merchant_status (merchant_id, status),
    INDEX idx_pce_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台抽成明细';

CREATE TABLE IF NOT EXISTS platform_commission_settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    settlement_no VARCHAR(32) NOT NULL UNIQUE,
    merchant_id BIGINT NOT NULL,
    period_type VARCHAR(20) NOT NULL COMMENT 'DAY/MONTH/YEAR/CUSTOM',
    period_start DATETIME NOT NULL,
    period_end DATETIME NOT NULL COMMENT '不含右端或按闭区间在业务层约定',
    entry_count INT NOT NULL DEFAULT 0,
    order_amount_sum DECIMAL(14,2) NOT NULL DEFAULT 0,
    commission_sum DECIMAL(14,2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'SETTLED',
    voucher_no VARCHAR(64),
    remark VARCHAR(500),
    snapshot_json MEDIUMTEXT NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(50),
    settled_at DATETIME NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_pcs_merchant (merchant_id),
    INDEX idx_pcs_settled (settled_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='平台抽成结算单（含快照）';
