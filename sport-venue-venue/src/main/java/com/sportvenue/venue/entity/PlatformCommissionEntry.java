package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_commission_entries",
        uniqueConstraints = @UniqueConstraint(name = "uk_pce_biz", columnNames = {"biz_type", "biz_id"}))
@EntityListeners(AuditingEntityListener.class)
public class PlatformCommissionEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "biz_type", nullable = false, length = 30)
    private String bizType;

    @Column(name = "biz_id", nullable = false)
    private Long bizId;

    @Column(name = "order_no", length = 32)
    private String orderNo;

    @Column(name = "pay_method", nullable = false, length = 30)
    private String payMethod;

    @Column(name = "order_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal orderAmount;

    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rate;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EntryStatus status = EntryStatus.PENDING;

    @Column(name = "settlement_id")
    private Long settlementId;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public enum EntryStatus {
        PENDING, SETTLED
    }

    public static final String BIZ_SALES_ORDER = "SALES_ORDER";
}
