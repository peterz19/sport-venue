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

/**
 * 场馆价格策略实体类
 * 支持复杂的价格策略：时段定价、会员折扣、节假日价格等
 */
@Data
@Entity
@Table(name = "venue_prices")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class VenuePrice {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 场馆ID
     */
    @Column(nullable = false)
    private Long venueId;
    
    /**
     * 价格策略名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 价格策略类型：HOURLY(按小时), DAILY(按天), PACKAGE(套餐), MEMBER(会员价)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType type;
    
    /**
     * 基础价格
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;
    
    /**
     * 会员价格
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal memberPrice;
    
    /**
     * VIP价格
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal vipPrice;
    
    /**
     * 节假日价格
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal holidayPrice;
    
    /**
     * 时段开始时间（HH:mm格式）
     */
    @Column(length = 10)
    private String timeSlotStart;
    
    /**
     * 时段结束时间（HH:mm格式）
     */
    @Column(length = 10)
    private String timeSlotEnd;
    
    /**
     * 适用星期（1-7，逗号分隔）
     */
    @Column(length = 20)
    private String weekdays;
    
    /**
     * 适用日期范围开始
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime validFrom;
    
    /**
     * 适用日期范围结束
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDateTime validTo;
    
    /**
     * 最小使用时长（小时）
     */
    private Integer minDuration;
    
    /**
     * 最大使用时长（小时）
     */
    private Integer maxDuration;
    
    /**
     * 是否启用
     */
    private Boolean enabled = true;
    
    /**
     * 优先级（数字越大优先级越高）
     */
    private Integer priority = 0;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
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
     * 价格策略类型枚举
     */
    public enum PriceType {
        HOURLY("按小时"),
        DAILY("按天"),
        PACKAGE("套餐"),
        MEMBER("会员价"),
        PEAK("高峰价"),
        OFF_PEAK("低峰价");
        
        private final String description;
        
        PriceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 