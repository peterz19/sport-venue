package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "merchant_wx_channels",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_mwc_app", columnNames = "app_id"),
                @UniqueConstraint(name = "uk_mwc_merchant_type", columnNames = {"merchant_id", "channel_type"})
        })
@EntityListeners(AuditingEntityListener.class)
public class MerchantWxChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Enumerated(EnumType.STRING)
    @Column(name = "channel_type", nullable = false, length = 20)
    private ChannelType channelType;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(name = "app_secret_enc", length = 512)
    private String appSecretEnc;

    @Column(name = "oa_server_token", length = 64)
    private String oaServerToken;

    @Column(name = "oa_encoding_aes_key", length = 64)
    private String oaEncodingAesKey;

    @Enumerated(EnumType.STRING)
    @Column(name = "bind_status", nullable = false, length = 20)
    private BindStatus bindStatus = BindStatus.UNSET;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_type", nullable = false, length = 20)
    private AuthType authType = AuthType.SELF;

    @Column(length = 200)
    private String remark;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;

    @LastModifiedDate
    @Column(name = "update_time")
    private LocalDateTime updateTime;

    @Column(name = "update_by")
    private Long updateBy;

    public enum ChannelType {
        MINI_PROGRAM, OFFICIAL_ACCOUNT
    }

    public enum BindStatus {
        UNSET, BOUND, INVALID
    }

    public enum AuthType {
        SELF, COMPONENT
    }
}
