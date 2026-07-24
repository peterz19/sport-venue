-- ============================================================
-- Phase 1: B端轻量收银模块（无库存）
-- ============================================================

USE sport_venue;

CREATE TABLE IF NOT EXISTS products (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id     BIGINT          NOT NULL                COMMENT '所属商户',
    venue_id        BIGINT                                  COMMENT '所属场馆（NULL=商户通用）',
    name            VARCHAR(100)    NOT NULL                COMMENT '商品名称',
    price           DECIMAL(10, 2)  NOT NULL                COMMENT '单价（元）',
    unit            VARCHAR(20)     NOT NULL DEFAULT '个'   COMMENT '单位',
    category        VARCHAR(50)                             COMMENT '分类',
    sort_order      INT             NOT NULL DEFAULT 0      COMMENT '排序',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ON_SALE' COMMENT 'ON_SALE/OFF_SALE',
    deleted         TINYINT(1)      NOT NULL DEFAULT 0      COMMENT '软删除',
    remark          VARCHAR(200)                            COMMENT '备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_venue_id (venue_id),
    INDEX idx_status (status),
    INDEX idx_merchant_status (merchant_id, status, deleted)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='零售商品表（无库存）';

CREATE TABLE IF NOT EXISTS sales_orders (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no        VARCHAR(32)     NOT NULL                COMMENT '销售单号',
    merchant_id     BIGINT          NOT NULL                COMMENT '商户ID',
    venue_id        BIGINT          NOT NULL                COMMENT '场馆ID',
    total_amount    DECIMAL(10, 2)  NOT NULL                COMMENT '订单总金额',
    item_count      INT             NOT NULL DEFAULT 0      COMMENT '商品种类数',
    total_qty       INT             NOT NULL DEFAULT 0      COMMENT '商品总件数',
    status          VARCHAR(20)     NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING/PAID/CANCELLED',
    pay_method      VARCHAR(30)                             COMMENT 'CASH/WECHAT/ALIPAY',
    operator_id     BIGINT                                  COMMENT '操作员用户ID',
    operator_name   VARCHAR(50)                             COMMENT '操作员姓名快照',
    paid_at         DATETIME                                COMMENT '支付完成时间',
    cancelled_at    DATETIME                                COMMENT '取消时间',
    cancel_reason   VARCHAR(200)                            COMMENT '取消原因',
    remark          VARCHAR(200)                            COMMENT '备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_merchant_id (merchant_id),
    INDEX idx_venue_id (venue_id),
    INDEX idx_status (status),
    INDEX idx_paid_at (paid_at),
    INDEX idx_merchant_paid (merchant_id, paid_at),
    INDEX idx_merchant_create (merchant_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售单';

CREATE TABLE IF NOT EXISTS sales_order_items (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL                COMMENT '销售单ID',
    product_id      BIGINT                                  COMMENT '商品ID',
    product_name    VARCHAR(100)    NOT NULL                COMMENT '商品名称快照',
    unit            VARCHAR(20)     NOT NULL DEFAULT '个'   COMMENT '单位快照',
    unit_price      DECIMAL(10, 2)  NOT NULL                COMMENT '单价快照',
    quantity        INT             NOT NULL                COMMENT '数量',
    subtotal        DECIMAL(10, 2)  NOT NULL                COMMENT '小计',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_order_id (order_id),
    INDEX idx_product_id (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='销售明细';

CREATE TABLE IF NOT EXISTS sales_payments (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id        BIGINT          NOT NULL                COMMENT '销售单ID',
    payment_no      VARCHAR(32)     NOT NULL                COMMENT '支付流水号',
    pay_method      VARCHAR(30)     NOT NULL                COMMENT 'CASH/WECHAT/ALIPAY',
    amount          DECIMAL(10, 2)  NOT NULL                COMMENT '支付金额',
    status          VARCHAR(20)     NOT NULL DEFAULT 'SUCCESS' COMMENT 'PENDING/SUCCESS/FAILED',
    third_party_no  VARCHAR(64)                             COMMENT '第三方流水号',
    paid_at         DATETIME        NOT NULL                COMMENT '支付时间',
    remark          VARCHAR(200)                            COMMENT '备注',
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_payment_no (payment_no),
    INDEX idx_order_id (order_id),
    INDEX idx_paid_at (paid_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付流水';
