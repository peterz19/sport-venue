package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_wx_identities",
        uniqueConstraints = @UniqueConstraint(name = "uk_uwi_app_openid", columnNames = {"app_id", "openid"}))
@EntityListeners(AuditingEntityListener.class)
public class UserWxIdentity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "merchant_id", nullable = false)
    private Long merchantId;

    @Column(name = "channel_type", nullable = false, length = 20)
    private String channelType;

    @Column(name = "app_id", nullable = false, length = 64)
    private String appId;

    @Column(nullable = false, length = 64)
    private String openid;

    @Column(length = 64)
    private String unionid;

    @Column(name = "customer_user_id", nullable = false)
    private Long customerUserId;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
