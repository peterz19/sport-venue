package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * 角色实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "roles")
@EntityListeners(AuditingEntityListener.class)
public class Role {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 角色编码
     */
    @Column(unique = true, nullable = false, length = 50)
    private String code;
    
    /**
     * 角色名称
     */
    @Column(nullable = false, length = 50)
    private String name;
    
    /**
     * 角色描述
     */
    @Column(length = 200)
    private String description;
    
    /**
     * 角色类型：SYSTEM(系统角色), MERCHANT(商户角色), CUSTOM(自定义角色)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleType roleType;
    
    /**
     * 所属商户ID（商户角色）
     */
    private Long merchantId;
    
    /**
     * 角色状态：ACTIVE(正常), INACTIVE(停用)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoleStatus status = RoleStatus.ACTIVE;
    
    /**
     * 排序
     */
    private Integer sort = 0;
    
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
     * 角色权限关联
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;
    
    /**
     * 角色类型枚举
     */
    public enum RoleType {
        SYSTEM("系统角色"),
        MERCHANT("商户角色"),
        CUSTOM("自定义角色");
        
        private final String description;
        
        RoleType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 角色状态枚举
     */
    public enum RoleStatus {
        ACTIVE("正常"),
        INACTIVE("停用");
        
        private final String description;
        
        RoleStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 