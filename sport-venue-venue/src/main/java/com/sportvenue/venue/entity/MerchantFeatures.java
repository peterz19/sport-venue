package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchant_features")
public class MerchantFeatures {

    @Id
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "enable_cashier", nullable = false)
    private Boolean enableCashier = true;

    @Column(name = "enable_booking", nullable = false)
    private Boolean enableBooking = true;

    @Column(name = "enable_team_match", nullable = false)
    private Boolean enableTeamMatch = true;

    @Column(name = "enable_c_end", nullable = false)
    private Boolean enableCEnd = false;

    @Column(name = "enable_recharge", nullable = false)
    private Boolean enableRecharge = false;

    @Column(name = "max_staff", nullable = false)
    private Integer maxStaff = 50;

    @Column(name = "max_venues", nullable = false)
    private Integer maxVenues = 20;

    @Column(name = "max_courts", nullable = false)
    private Integer maxCourts = 100;

    @Column(name = "max_wx_mini", nullable = false)
    private Integer maxWxMini = 1;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    public static MerchantFeatures defaults(Long merchantId) {
        MerchantFeatures f = new MerchantFeatures();
        f.setMerchantId(merchantId);
        f.setEnableCashier(true);
        f.setEnableBooking(true);
        f.setEnableTeamMatch(true);
        f.setEnableCEnd(false);
        f.setEnableRecharge(false);
        f.setMaxStaff(50);
        f.setMaxVenues(20);
        f.setMaxCourts(100);
        f.setMaxWxMini(1);
        f.setUpdateTime(LocalDateTime.now());
        return f;
    }
}
