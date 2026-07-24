-- ============================================================
-- P1-P3: 片场 / 球队 / 订场 / 赛果（C端字段仅预留）
-- ============================================================

USE sport_venue;

CREATE TABLE IF NOT EXISTS courts (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id     BIGINT          NOT NULL                COMMENT '所属商户',
    venue_id        BIGINT          NOT NULL                COMMENT '所属场馆',
    name            VARCHAR(100)    NOT NULL                COMMENT '片场名称',
    code            VARCHAR(50)                             COMMENT '编号',
    court_type      VARCHAR(20)     NOT NULL DEFAULT 'FULL' COMMENT 'FULL/HALF/OTHER',
    status          VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    sort_order      INT             NOT NULL DEFAULT 0,
    remark          VARCHAR(200),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by       BIGINT,
    update_by       BIGINT,
    INDEX idx_merchant (merchant_id),
    INDEX idx_venue (venue_id),
    INDEX idx_merchant_venue (merchant_id, venue_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='片场';

CREATE TABLE IF NOT EXISTS teams (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    merchant_id         BIGINT          NOT NULL,
    name                VARCHAR(100)    NOT NULL                COMMENT '队名',
    captain_name        VARCHAR(50)     NOT NULL                COMMENT '队长/联系人',
    phone               VARCHAR(20)     NOT NULL,
    remark              VARCHAR(500),
    liaison_staff_id    BIGINT          NOT NULL                COMMENT '当前对接员工',
    liaison_staff_name  VARCHAR(50)                             COMMENT '对接员工姓名快照(当前)',
    status              VARCHAR(20)     NOT NULL DEFAULT 'ACTIVE' COMMENT 'ACTIVE/INACTIVE',
    create_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time         DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    create_by           BIGINT,
    update_by           BIGINT,
    INDEX idx_merchant (merchant_id),
    INDEX idx_liaison (liaison_staff_id),
    INDEX idx_merchant_status (merchant_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商户客户球队';

CREATE TABLE IF NOT EXISTS team_audit_logs (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    team_id         BIGINT          NOT NULL,
    merchant_id     BIGINT          NOT NULL,
    action          VARCHAR(40)     NOT NULL                COMMENT 'CREATE/UPDATE/CHANGE_LIAISON/DISABLE/ENABLE',
    before_json     TEXT,
    after_json      TEXT,
    reason          VARCHAR(200),
    operator_id     BIGINT,
    operator_name   VARCHAR(50),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_team (team_id),
    INDEX idx_merchant (merchant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='球队变更审计';

CREATE TABLE IF NOT EXISTS bookings (
    id                      BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_no                VARCHAR(32)     NOT NULL,
    merchant_id             BIGINT          NOT NULL,
    venue_id                BIGINT          NOT NULL,
    court_id                BIGINT          NOT NULL,
    start_time              DATETIME        NOT NULL,
    end_time                DATETIME        NOT NULL,
    book_type               VARCHAR(20)     NOT NULL                COMMENT 'TEAM/PERSON',
    team_id                 BIGINT,
    team_name               VARCHAR(100)                            COMMENT '球队名快照',
    person_name             VARCHAR(50),
    person_phone            VARCHAR(20),
    operator_id             BIGINT          NOT NULL,
    operator_name           VARCHAR(50),
    liaison_staff_id        BIGINT                                  COMMENT '对接员工快照',
    liaison_staff_name      VARCHAR(50),
    status                  VARCHAR(20)     NOT NULL DEFAULT 'BOOKED' COMMENT 'BOOKED/COMPLETED/CANCELLED/EXPIRED',
    amount                  DECIMAL(10, 2)  NOT NULL DEFAULT 0.00,
    source                  VARCHAR(10)     NOT NULL DEFAULT 'B'     COMMENT 'B/C 预留',
    customer_user_id        BIGINT                                  COMMENT 'C端用户预留',
    remark                  VARCHAR(200),
    cancelled_at            DATETIME,
    cancel_reason           VARCHAR(200),
    create_time             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time             DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_order_no (order_no),
    INDEX idx_merchant (merchant_id),
    INDEX idx_court_time (court_id, start_time, end_time),
    INDEX idx_status (status),
    INDEX idx_operator (operator_id),
    INDEX idx_liaison (liaison_staff_id),
    INDEX idx_merchant_create (merchant_id, create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='片场订场单';

CREATE TABLE IF NOT EXISTS match_results (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_id      BIGINT          NOT NULL,
    merchant_id     BIGINT          NOT NULL,
    home_team_id    BIGINT          NOT NULL,
    away_team_id    BIGINT          NOT NULL,
    home_score      INT             NOT NULL,
    away_score      INT             NOT NULL,
    result          VARCHAR(20)     NOT NULL                COMMENT 'HOME_WIN/AWAY_WIN/DRAW',
    operator_id     BIGINT,
    operator_name   VARCHAR(50),
    remark          VARCHAR(200),
    create_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time     DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_booking (booking_id),
    INDEX idx_merchant (merchant_id),
    INDEX idx_home (home_team_id),
    INDEX idx_away (away_team_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订场赛果';
