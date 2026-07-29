package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "customer_wallets",
        uniqueConstraints = @UniqueConstraint(name = "uk_cw_merchant_customer",
                columnNames = {"merchant_id", "customer_user_id"}))
public class CustomerWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "customer_user_id", nullable = false)
    private Long customerUserId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(name = "update_time")
    private LocalDateTime updateTime;
}
