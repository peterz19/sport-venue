package com.sportvenue.venue.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 权限实体
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "permissions")
@EntityListeners(AuditingEntityListener.class)
public class Permission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 权限编码
     */
    @Column(unique = true, nullable = false, length = 100)
    private String code;
    
    /**
     * 权限名称
     */
    @Column(nullable = false, length = 100)
    private String name;
    
    /**
     * 权限描述
     */
    @Column(length = 200)
    private String description;
    
    /**
     * 权限类型：MENU(菜单), BUTTON(按钮), API(接口)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionType permissionType;
    
    /**
     * 权限分类：VENUE(场馆), RESERVATION(预约), CHECKIN(打卡), USER(用户), SYSTEM(系统)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionCategory category;
    
    /**
     * 资源路径（API路径或菜单路径）
     */
    @Column(length = 200)
    private String resourcePath;
    
    /**
     * HTTP方法（GET, POST, PUT, DELETE等）
     */
    @Column(length = 10)
    private String httpMethod;
    
    /**
     * 父权限ID
     */
    private Long parentId;
    
    /**
     * 排序
     */
    private Integer sort = 0;
    
    /**
     * 权限状态：ACTIVE(正常), INACTIVE(停用)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionStatus status = PermissionStatus.ACTIVE;
    
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
     * 权限类型枚举
     */
    public enum PermissionType {
        MENU("菜单"),
        BUTTON("按钮"),
        API("接口");
        
        private final String description;
        
        PermissionType(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 权限分类枚举
     */
    public enum PermissionCategory {
        VENUE("场馆管理"),
        RESERVATION("预约管理"),
        CHECKIN("打卡管理"),
        USER("用户管理"),
        SYSTEM("系统管理"),
        STATISTICS("统计分析");
        
        private final String description;
        
        PermissionCategory(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    /**
     * 权限状态枚举
     */
    public enum PermissionStatus {
        ACTIVE("正常"),
        INACTIVE("停用");
        
        private final String description;
        
        PermissionStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
} 