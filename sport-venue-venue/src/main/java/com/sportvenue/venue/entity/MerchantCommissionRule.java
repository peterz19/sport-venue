package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchant_commission_rules")
public class MerchantCommissionRule {

    @Id
    @Column(name = "merchant_id")
    private Long merchantId;

    /** 抽成比例，如 0.03 = 3% */
    @Column(nullable = false, precision = 8, scale = 4)
    private BigDecimal rate = BigDecimal.ZERO;

    @Column(name = "include_cash", nullable = false)
    private Boolean includeCash = false;

    @Column(name = "include_wechat", nullable = false)
    private Boolean includeWechat = true;

    @Column(name = "include_alipay", nullable = false)
    private Boolean includeAlipay = true;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(length = 200)
    private String remark;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "update_by")
    private Long updateBy;

    public static MerchantCommissionRule defaults(Long merchantId) {
        MerchantCommissionRule r = new MerchantCommissionRule();
        r.setMerchantId(merchantId);
        r.setRate(BigDecimal.ZERO);
        r.setIncludeCash(false);
        r.setIncludeWechat(true);
        r.setIncludeAlipay(true);
        r.setEnabled(true);
        r.setUpdateTime(LocalDateTime.now());
        return r;
    }
}
