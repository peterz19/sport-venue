package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "wallet_ledgers")
@EntityListeners(AuditingEntityListener.class)
public class WalletLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "customer_user_id", nullable = false)
    private Long customerUserId;

    @Column(name = "change_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal changeAmount;

    @Column(name = "balance_after", nullable = false, precision = 12, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "biz_type", nullable = false, length = 30)
    private String bizType;

    @Column(name = "biz_id")
    private Long bizId;

    @Column(length = 200)
    private String remark;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
