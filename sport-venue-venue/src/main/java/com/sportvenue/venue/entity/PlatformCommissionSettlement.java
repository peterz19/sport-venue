package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_commission_settlements")
@EntityListeners(AuditingEntityListener.class)
public class PlatformCommissionSettlement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "settlement_no", nullable = false, unique = true, length = 32)
    private String settlementNo;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 20)
    private PeriodType periodType;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "entry_count", nullable = false)
    private Integer entryCount = 0;

    @Column(name = "order_amount_sum", nullable = false, precision = 14, scale = 2)
    private BigDecimal orderAmountSum = BigDecimal.ZERO;

    @Column(name = "commission_sum", nullable = false, precision = 14, scale = 2)
    private BigDecimal commissionSum = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SettlementStatus status = SettlementStatus.SETTLED;

    @Column(name = "voucher_no", length = 64)
    private String voucherNo;

    @Column(length = 500)
    private String remark;

    @Lob
    @Column(name = "snapshot_json", nullable = false, columnDefinition = "MEDIUMTEXT")
    private String snapshotJson;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(name = "settled_at", nullable = false)
    private LocalDateTime settledAt;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    public enum PeriodType {
        DAY, MONTH, YEAR, CUSTOM
    }

    public enum SettlementStatus {
        SETTLED, CANCELLED
    }
}
