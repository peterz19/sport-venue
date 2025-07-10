package com.sportvenue.venue.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * 场馆打卡实体类
 * 支持打卡积分功能、签到记录
 */
@Data
@Entity
@Table(name = "venue_check_ins")
@EntityListeners(AuditingEntityListener.class)
@EqualsAndHashCode(callSuper = false)
public class VenueCheckIn {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 打卡编号
     */
    @Column(nullable = false, unique = true, length = 50)
    private String checkInNo;
    
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
     * 预约ID（如果有预约）
     */
    private Long reservationId;
    
    /**
     * 打卡时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
    
    /**
     * 打卡类型：CHECK_IN(入场), CHECK_OUT(离场), AUTO_CHECK_OUT(自动离场)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInType type = CheckInType.CHECK_IN;
    
    /**
     * 打卡方式：QR_CODE(二维码), NFC(NFC), MANUAL(手动), AUTO(自动)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CheckInMethod method = CheckInMethod.QR_CODE;
    
    /**
     * 打卡位置（GPS坐标）
     */
    @Column(length = 100)
    private String location;
    
    /**
     * 设备ID
     */
    @Column(length = 100)
    private String deviceId;
    
    /**
     * 设备类型：MOBILE(手机), TABLET(平板), TERMINAL(终端)
     */
    @Enumerated(EnumType.STRING)
    private DeviceType deviceType;
    
    /**
     * 获得积分
     */
    private Integer earnedPoints = 0;
    
    /**
     * 连续打卡天数
     */
    private Integer consecutiveDays = 0;
    
    /**
     * 总打卡次数
     */
    private Integer totalCheckIns = 0;
    
    /**
     * 使用时长（分钟）
     */
    private Integer durationMinutes = 0;
    
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
     * 打卡类型枚举
     */
    public enum CheckInType {
        CHECK_IN("入场"),
        CHECK_OUT("离场"),
        AUTO_CHECK_OUT("自动离场"),
        RE_CHECK_IN("重新入场");
        
        private final String description;
        
        CheckInType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 打卡方式枚举
     */
    public enum CheckInMethod {
        QR_CODE("二维码"),
        NFC("NFC"),
        MANUAL("手动"),
        AUTO("自动"),
        FACE("人脸识别");
        
        private final String description;
        
        CheckInMethod(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 设备类型枚举
     */
    public enum DeviceType {
        MOBILE("手机"),
        TABLET("平板"),
        TERMINAL("终端"),
        PC("电脑");
        
        private final String description;
        
        DeviceType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 