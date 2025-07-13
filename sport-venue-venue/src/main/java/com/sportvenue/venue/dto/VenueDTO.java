package com.sportvenue.venue.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sportvenue.venue.entity.Venue;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆数据传输对象
 */
@Data
public class VenueDTO {
    
    private Long id;
    private String name;
    private String description;
    private Venue.VenueType type;
    private String typeDescription;
    private Venue.VenueSpaceType spaceType;
    private String spaceTypeDescription;
    private Venue.VenueChargeType chargeType;
    private String chargeTypeDescription;
    private Long merchantId;
    private String merchantName;
    private String address;
    private BigDecimal longitude;
    private BigDecimal latitude;
    private String phone;
    private String openTime;
    private String closeTime;
    private Venue.VenueStatus status;
    private String statusDescription;
    private Integer capacity;
    private Integer currentOccupancy;
    private BigDecimal area;
    private String facilities;
    private List<String> images;
    private List<String> tags;
    private BigDecimal rating;
    private Integer ratingCount;
    private Boolean reservationEnabled;
    private Boolean checkInEnabled;
    private Boolean pointsEnabled;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;
    
    // 扩展字段
    private BigDecimal distance; // 距离（米）
    private Boolean isOnline; // 是否在线
    private Integer onlineUsers; // 在线用户数
    private BigDecimal minPrice; // 最低价格
    private BigDecimal maxPrice; // 最高价格
    private Boolean isFavorite; // 是否收藏
    private Integer reservationCount; // 预约数量
    private Integer checkInCount; // 打卡数量
    
    /**
     * 从实体转换为DTO
     */
    public static VenueDTO fromEntity(Venue venue) {
        VenueDTO dto = new VenueDTO();
        dto.setId(venue.getId());
        dto.setName(venue.getName());
        dto.setDescription(venue.getDescription());
        dto.setType(venue.getType());
        dto.setTypeDescription(venue.getType().getDescription());
        dto.setSpaceType(venue.getSpaceType());
        dto.setSpaceTypeDescription(venue.getSpaceType().getDescription());
        dto.setChargeType(venue.getChargeType());
        dto.setChargeTypeDescription(venue.getChargeType().getDescription());
        dto.setMerchantId(venue.getMerchantId());
        dto.setMerchantName(venue.getMerchantName());
        dto.setAddress(venue.getAddress());
        dto.setLongitude(venue.getLongitude());
        dto.setLatitude(venue.getLatitude());
        dto.setPhone(venue.getPhone());
        dto.setOpenTime(venue.getOpenTime());
        dto.setCloseTime(venue.getCloseTime());
        dto.setStatus(venue.getStatus());
        dto.setStatusDescription(venue.getStatus().getDescription());
        dto.setCapacity(venue.getCapacity());
        dto.setCurrentOccupancy(venue.getCurrentOccupancy());
        dto.setArea(venue.getArea());
        dto.setFacilities(venue.getFacilities());
        dto.setRating(venue.getRating());
        dto.setRatingCount(venue.getRatingCount());
        dto.setReservationEnabled(venue.getReservationEnabled());
        dto.setCheckInEnabled(venue.getCheckInEnabled());
        dto.setPointsEnabled(venue.getPointsEnabled());
        dto.setCreateTime(venue.getCreateTime());
        dto.setUpdateTime(venue.getUpdateTime());
        
        return dto;
    }
} 