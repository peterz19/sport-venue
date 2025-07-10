package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商户实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "merchants")
@EntityListeners(AuditingEntityListener.class)
public class Merchant {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 商户编码
     */
    @Column(unique = true, nullable = false, length = 50)
    private String merchantCode;
    
    /**
     * 商户名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 商户简称
     */
    @Column(length = 50)
    private String shortName;
    
    /**
     * 商户类型：INDIVIDUAL(个人), COMPANY(企业), CHAIN(连锁)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantType merchantType;
    
    /**
     * 商户状态：ACTIVE(正常), INACTIVE(停用), SUSPENDED(暂停)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MerchantStatus status = MerchantStatus.ACTIVE;
    
    /**
     * 联系人姓名
     */
    @Column(length = 50)
    private String contactName;
    
    /**
     * 联系人手机号
     */
    @Column(length = 20)
    private String contactPhone;
    
    /**
     * 联系人邮箱
     */
    @Column(length = 100)
    private String contactEmail;
    
    /**
     * 商户地址
     */
    @Column(length = 200)
    private String address;
    
    /**
     * 营业执照号
     */
    @Column(length = 50)
    private String businessLicense;
    
    /**
     * 法人姓名
     */
    @Column(length = 50)
    private String legalPerson;
    
    /**
     * 法人身份证号
     */
    @Column(length = 20)
    private String legalPersonId;
    
    /**
     * 注册资本
     */
    private BigDecimal registeredCapital;
    
    /**
     * 成立日期
     */
    private LocalDateTime establishmentDate;
    
    /**
     * 营业时间
     */
    @Column(length = 100)
    private String businessHours;
    
    /**
     * 商户简介
     */
    @Column(length = 1000)
    private String description;
    
    /**
     * 商户logo
     */
    @Column(length = 200)
    private String logo;
    
    /**
     * 商户图片
     */
    @Column(length = 1000)
    private String images;
    
    /**
     * 商户标签
     */
    @Column(length = 500)
    private String tags;
    
    /**
     * 评分
     */
    private BigDecimal rating = BigDecimal.ZERO;
    
    /**
     * 评分人数
     */
    private Integer ratingCount = 0;
    
    /**
     * 场馆数量
     */
    private Integer venueCount = 0;
    
    /**
     * 总营业额
     */
    private BigDecimal totalRevenue = BigDecimal.ZERO;
    
    /**
     * 本月营业额
     */
    private BigDecimal monthlyRevenue = BigDecimal.ZERO;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    /**
     * 创建时间
     */
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(nullable = false)
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
     * 商户类型枚举
     */
    public enum MerchantType {
        INDIVIDUAL("个人"),
        COMPANY("企业"),
        CHAIN("连锁");
        
        private final String description;
        
        MerchantType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 商户状态枚举
     */
    public enum MerchantStatus {
        ACTIVE("正常"),
        INACTIVE("停用"),
        SUSPENDED("暂停");
        
        private final String description;
        
        MerchantStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 