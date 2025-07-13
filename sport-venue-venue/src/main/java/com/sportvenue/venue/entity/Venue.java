package com.sportvenue.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆实体类
 * 支持多商户、多类型场馆管理
 */
@Data
@Entity
@Table(name = "venues")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class Venue {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 场馆名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 场馆描述
     */
    @Column(length = 500)
    private String description;
    
    /**
     * 场馆类型：PARK(公园), INSTITUTION(机构), STADIUM(体育场), GYM(健身房)等
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueType type;

    /**
     * 空间类型：INDOOR(室内), OUTDOOR(室外)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueSpaceType spaceType;

    /**
     * 付费类型：PAID(付费), FREE(免费)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueChargeType chargeType;
    
    /**
     * 商户ID（支持多商户）
     */
    @Column(nullable = false)
    private Long merchantId;
    
    /**
     * 商户名称
     */
    @Column(length = 100)
    private String merchantName;
    
    /**
     * 场馆地址
     */
    @Column(nullable = false, length = 200)
    private String address;
    
    /**
     * 经度
     */
    private BigDecimal longitude;
    
    /**
     * 纬度
     */
    private BigDecimal latitude;
    
    /**
     * 联系电话
     */
    @Column(length = 20)
    private String phone;
    
    /**
     * 营业时间开始
     */
    @Column(length = 10)
    private String openTime;
    
    /**
     * 营业时间结束
     */
    @Column(length = 10)
    private String closeTime;
    
    /**
     * 场馆状态：ACTIVE(正常), INACTIVE(停用), MAINTENANCE(维护中)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VenueStatus status = VenueStatus.ACTIVE;
    
    /**
     * 场馆容量（最大人数）
     */
    private Integer capacity;
    
    /**
     * 当前使用人数
     */
    private Integer currentOccupancy = 0;
    
    /**
     * 场馆面积（平方米）
     */
    private BigDecimal area;
    
    /**
     * 场馆设施描述
     */
    @Column(length = 500)
    private String facilities;
    
    /**
     * 场馆图片URL列表（JSON格式存储）
     */
    @Column(columnDefinition = "TEXT")
    private String images;
    
    /**
     * 场馆标签（JSON格式存储）
     */
    @Column(columnDefinition = "TEXT")
    private String tags;
    
    /**
     * 评分（1-5分）
     */
    private BigDecimal rating = BigDecimal.ZERO;
    
    /**
     * 评分人数
     */
    private Integer ratingCount = 0;
    
    /**
     * 是否支持预约
     */
    private Boolean reservationEnabled = true;
    
    /**
     * 是否支持打卡
     */
    private Boolean checkInEnabled = true;
    
    /**
     * 是否支持积分
     */
    private Boolean pointsEnabled = true;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    /**
     * 创建人ID
     */
    private Long createBy;
    
    /**
     * 更新人ID
     */
    private Long updateBy;
    
    /**
     * 场馆类型枚举（只保留场所属性）
     */
    public enum VenueType {
        PARK("公园"),
        INSTITUTION("机构"),
        STADIUM("体育场"),
        GYM("健身房"),
        OTHER("其他");
        private final String description;
        VenueType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /**
     * 空间类型枚举
     */
    public enum VenueSpaceType {
        INDOOR("室内"),
        OUTDOOR("室外");
        private final String description;
        VenueSpaceType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }

    /**
     * 付费类型枚举
     */
    public enum VenueChargeType {
        PAID("付费"),
        FREE("免费");
        private final String description;
        VenueChargeType(String description) { this.description = description; }
        public String getDescription() { return description; }
    }
    
    /**
     * 场馆状态枚举
     */
    public enum VenueStatus {
        ACTIVE("正常"),
        INACTIVE("停用"),
        MAINTENANCE("维护中");
        
        private final String description;
        
        VenueStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 