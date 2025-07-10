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
 * 场馆预约实体类
 * 支持预约管理、状态跟踪、支付集成
 */
@Data
@Entity
@Table(name = "venue_reservations")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class VenueReservation {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 预约编号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String reservationNo;
    
    /**
     * 场馆ID
     */
    @Column(nullable = false)
    private Long venueId;
    
    /**
     * 用户ID
     */
    @Column(nullable = false)
    private Long userId;
    
    /**
     * 用户姓名
     */
    @Column(length = 50)
    private String userName;
    
    /**
     * 用户手机号
     */
    @Column(length = 20)
    private String userPhone;
    
    /**
     * 预约开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime startTime;
    
    /**
     * 预约结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime endTime;
    
    /**
     * 使用时长（小时）
     */
    private Integer duration;
    
    /**
     * 预约人数
     */
    private Integer peopleCount = 1;
    
    /**
     * 预约状态：PENDING(待确认), CONFIRMED(已确认), CANCELLED(已取消), COMPLETED(已完成)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status = ReservationStatus.PENDING;
    
    /**
     * 预约类型：NORMAL(普通预约), MEMBER(会员预约), VIP(VIP预约)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationType type = ReservationType.NORMAL;
    
    /**
     * 单价
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice;
    
    /**
     * 总价
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal totalPrice;
    
    /**
     * 折扣金额
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;
    
    /**
     * 实付金额
     */
    @Column(precision = 10, scale = 2)
    private BigDecimal actualAmount;
    
    /**
     * 支付状态：UNPAID(未支付), PAID(已支付), REFUNDED(已退款)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus paymentStatus = PaymentStatus.UNPAID;
    
    /**
     * 支付方式：WECHAT(微信), ALIPAY(支付宝), CASH(现金), CARD(刷卡)
     */
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    /**
     * 支付时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime paymentTime;
    
    /**
     * 交易流水号
     */
    @Column(length = 100)
    private String transactionNo;
    
    /**
     * 备注
     */
    @Column(length = 500)
    private String remark;
    
    /**
     * 取消原因
     */
    @Column(length = 200)
    private String cancelReason;
    
    /**
     * 取消时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime cancelTime;
    
    /**
     * 取消人ID
     */
    private Long cancelBy;
    
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
     * 预约状态枚举
     */
    public enum ReservationStatus {
        PENDING("待确认"),
        CONFIRMED("已确认"),
        CANCELLED("已取消"),
        COMPLETED("已完成"),
        NO_SHOW("未到场");
        
        private final String description;
        
        ReservationStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 预约类型枚举
     */
    public enum ReservationType {
        NORMAL("普通预约"),
        MEMBER("会员预约"),
        VIP("VIP预约"),
        GROUP("团体预约");
        
        private final String description;
        
        ReservationType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 支付状态枚举
     */
    public enum PaymentStatus {
        UNPAID("未支付"),
        PAID("已支付"),
        REFUNDED("已退款"),
        PARTIAL_REFUND("部分退款");
        
        private final String description;
        
        PaymentStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 支付方式枚举
     */
    public enum PaymentMethod {
        WECHAT("微信"),
        ALIPAY("支付宝"),
        CASH("现金"),
        CARD("刷卡"),
        POINTS("积分抵扣");
        
        private final String description;
        
        PaymentMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 