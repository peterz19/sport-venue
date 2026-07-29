package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchant_wx_pay")
public class MerchantWxPay {

    @Id
    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "mch_id", length = 64)
    private String mchId;

    @Column(name = "mch_api_v3_key_enc", length = 512)
    private String mchApiV3KeyEnc;

    @Column(name = "mch_serial_no", length = 128)
    private String mchSerialNo;

    @Column(name = "notify_path", length = 100)
    private String notifyPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PayStatus status = PayStatus.INACTIVE;

    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "update_by")
    private Long updateBy;

    public enum PayStatus {
        ACTIVE, INACTIVE
    }
}
