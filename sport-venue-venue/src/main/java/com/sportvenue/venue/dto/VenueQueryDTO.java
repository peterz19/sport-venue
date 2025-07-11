package com.sportvenue.venue.dto;

import com.sportvenue.venue.entity.Venue;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 场馆查询条件DTO
 */
@Data
public class VenueQueryDTO {
    
    /**
     * 商户ID
     */
    private Long merchantId;
    
    /**
     * 场馆名称（模糊查询）
     */
    private String name;
    
    /**
     * 场馆地址（模糊查询）
     */
    private String address;
    
    /**
     * 场馆类型
     */
    private Venue.VenueType type;
    
    /**
     * 场馆子类型
     */
    private Venue.VenueSubType subType;
    
    /**
     * 场馆状态
     */
    private Venue.VenueStatus status;
    
    public void setStatus(Venue.VenueStatus status) {
        this.status = status;
    }
    
    /**
     * 最小评分
     */
    private BigDecimal minRating;
    
    /**
     * 最大评分
     */
    private BigDecimal maxRating;
    
    /**
     * 最小容量
     */
    private Integer minCapacity;
    
    /**
     * 最大容量
     */
    private Integer maxCapacity;
    
    /**
     * 是否支持预约
     */
    private Boolean reservationEnabled;
    
    /**
     * 是否支持打卡
     */
    private Boolean checkInEnabled;
    
    /**
     * 是否支持积分
     */
    private Boolean pointsEnabled;
    
    /**
     * 地理位置查询
     */
    private BigDecimal longitude;
    private BigDecimal latitude;
    private BigDecimal radius; // 搜索半径（米）
    
    /**
     * 分页参数
     */
    private Integer page = 0;
    private Integer size = 10;
    
    /**
     * 排序字段
     */
    private String sortBy = "createTime";
    
    /**
     * 排序方向：ASC, DESC
     */
    private String sortDirection = "DESC";
    
    /**
     * 是否只查询在线场馆
     */
    private Boolean onlineOnly = false;
    
    /**
     * 是否只查询有优惠的场馆
     */
    private Boolean hasDiscount = false;
    
    /**
     * 价格范围
     */
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    
    /**
     * 营业时间查询
     */
    private String openTime;
    private String closeTime;
    
    /**
     * 标签查询
     */
    private String tags;
    
    /**
     * 设施查询
     */
    private String facilities;
} 