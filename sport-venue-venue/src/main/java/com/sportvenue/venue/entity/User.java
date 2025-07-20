package com.sportvenue.venue.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 用户实体
 * 支持B端商户用户和C端普通用户
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 用户名
     */
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    /**
     * 密码（加密存储）
     */
    @Column(nullable = false, length = 100)
    private String password;
    
    /**
     * 真实姓名
     */
    @Column(length = 50)
    private String realName;
    
    /**
     * 手机号
     */
    @Column(unique = true, length = 20)
    private String phone;
    
    /**
     * 邮箱
     */
    @Column(unique = true, length = 100)
    private String email;
    
    /**
     * 用户类型：B_MERCHANT(B端商户), B_STAFF(B端员工), C_USER(C端用户), ADMIN(系统管理员)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserType userType;
    
    /**
     * 所属商户ID（B端用户）
     */
    private Long merchantId;
    
    /**
     * 商户名称（冗余字段）
     */
    @Column(length = 100)
    private String merchantName;
    
    /**
     * 用户状态：ACTIVE(正常), INACTIVE(停用), LOCKED(锁定)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;
    
    /**
     * 头像URL
     */
    @Column(length = 200)
    private String avatar;
    
    /**
     * 积分余额
     */
    private Integer points = 0;
    
    /**
     * 会员等级：BRONZE(青铜), SILVER(白银), GOLD(黄金), PLATINUM(铂金), DIAMOND(钻石)
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MemberLevel memberLevel = MemberLevel.BRONZE;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    @Column(length = 50)
    private String lastLoginIp;
    
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
     * 用户角色关联
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;
    
    /**
     * 用户类型枚举
     */
    public enum UserType {
        B_MERCHANT("B端商户"),
        B_STAFF("B端员工"),
        C_USER("C端用户"),
        ADMIN("系统管理员");
        
        private final String description;
        
        UserType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * JSON反序列化器 - 支持大小写不敏感
         */
        @JsonCreator
        public static UserType fromString(String value) {
            if (value == null) {
                return C_USER; // 默认值
            }
            
            // 转换为大写进行比较
            String upperValue = value.toUpperCase();
            
            switch (upperValue) {
                case "B_MERCHANT":
                case "MERCHANT":
                case "B端商户":
                    return B_MERCHANT;
                case "B_STAFF":
                case "STAFF":
                case "B端员工":
                    return B_STAFF;
                case "C_USER":
                case "USER":
                case "C端用户":
                    return C_USER;
                case "ADMIN":
                case "系统管理员":
                    return ADMIN;
                default:
                    // 如果无法识别，返回默认值
                    return C_USER;
            }
        }
        
        /**
         * JSON序列化器
         */
        @JsonValue
        public String toValue() {
            return this.name();
        }
    }
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        ACTIVE("正常"),
        INACTIVE("停用"),
        LOCKED("锁定");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
        
        /**
         * JSON反序列化器 - 支持大小写不敏感
         */
        @JsonCreator
        public static UserStatus fromString(String value) {
            if (value == null) {
                return ACTIVE; // 默认值
            }
            
            // 转换为大写进行比较
            String upperValue = value.toUpperCase();
            
            switch (upperValue) {
                case "ACTIVE":
                case "NORMAL":
                case "正常":
                    return ACTIVE;
                case "INACTIVE":
                case "DISABLED":
                case "停用":
                    return INACTIVE;
                case "LOCKED":
                case "锁定":
                    return LOCKED;
                default:
                    // 如果无法识别，返回默认值
                    return ACTIVE;
            }
        }
        
        /**
         * JSON序列化器
         */
        @JsonValue
        public String toValue() {
            return this.name();
        }
    }
    
    /**
     * 会员等级枚举
     */
    public enum MemberLevel {
        BRONZE("青铜", 0),
        SILVER("白银", 1000),
        GOLD("黄金", 5000),
        PLATINUM("铂金", 10000),
        DIAMOND("钻石", 50000);
        
        private final String description;
        private final Integer minPoints;
        
        MemberLevel(String description, Integer minPoints) {
            this.description = description;
            this.minPoints = minPoints;
        }
        
        public String getDescription() {
            return description;
        }
        
        public Integer getMinPoints() {
            return minPoints;
        }
        
        /**
         * JSON反序列化器 - 支持大小写不敏感
         */
        @JsonCreator
        public static MemberLevel fromString(String value) {
            if (value == null) {
                return BRONZE; // 默认值
            }
            
            // 转换为大写进行比较
            String upperValue = value.toUpperCase();
            
            switch (upperValue) {
                case "BRONZE":
                case "青铜":
                    return BRONZE;
                case "SILVER":
                case "白银":
                    return SILVER;
                case "GOLD":
                case "黄金":
                    return GOLD;
                case "PLATINUM":
                case "铂金":
                    return PLATINUM;
                case "DIAMOND":
                case "钻石":
                    return DIAMOND;
                default:
                    // 如果无法识别，返回默认值
                    return BRONZE;
            }
        }
        
        /**
         * JSON序列化器
         */
        @JsonValue
        public String toValue() {
            return this.name();
        }
    }
} 