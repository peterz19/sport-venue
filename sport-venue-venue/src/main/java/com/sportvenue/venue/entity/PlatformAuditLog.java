package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "platform_audit_logs")
@EntityListeners(AuditingEntityListener.class)
public class PlatformAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 40)
    private String action;

    @Column(name = "target_type", nullable = false, length = 20)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "merchant_id")
    private Long merchantId;

    @Column(name = "before_json", columnDefinition = "TEXT")
    private String beforeJson;

    @Column(name = "after_json", columnDefinition = "TEXT")
    private String afterJson;

    @Column(name = "operator_id")
    private Long operatorId;

    @Column(name = "operator_name", length = 50)
    private String operatorName;

    @Column(length = 200)
    private String remark;

    @CreatedDate
    @Column(name = "create_time", updatable = false)
    private LocalDateTime createTime;
}
