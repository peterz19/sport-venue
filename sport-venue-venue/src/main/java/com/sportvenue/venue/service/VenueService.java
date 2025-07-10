package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.VenueDTO;
import com.sportvenue.venue.dto.VenueQueryDTO;
import com.sportvenue.venue.entity.Venue;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * 场馆服务接口
 * 支持多商户、多类型场馆管理
 */
public interface VenueService {
    
    /**
     * 创建场馆
     */
    ApiResponse<VenueDTO> createVenue(Venue venue);
    
    /**
     * 更新场馆信息
     */
    ApiResponse<VenueDTO> updateVenue(Long id, Venue venue);
    
    /**
     * 删除场馆
     */
    ApiResponse<Void> deleteVenue(Long id);
    
    /**
     * 根据ID查询场馆
     */
    ApiResponse<VenueDTO> getVenueById(Long id);
    
    /**
     * 根据编号查询场馆
     */
    ApiResponse<VenueDTO> getVenueByNo(String venueNo);
    
    /**
     * 分页查询场馆列表
     */
    ApiResponse<Page<VenueDTO>> getVenueList(VenueQueryDTO queryDTO);
    
    /**
     * 根据商户ID查询场馆列表
     */
    ApiResponse<List<VenueDTO>> getVenuesByMerchant(Long merchantId);
    
    /**
     * 根据类型查询场馆列表
     */
    ApiResponse<List<VenueDTO>> getVenuesByType(Venue.VenueType type);
    
    /**
     * 根据子类型查询场馆列表
     */
    ApiResponse<List<VenueDTO>> getVenuesBySubType(Venue.VenueSubType subType);
    
    /**
     * 根据状态查询场馆列表
     */
    ApiResponse<List<VenueDTO>> getVenuesByStatus(Venue.VenueStatus status);
    
    /**
     * 搜索附近场馆
     */
    ApiResponse<List<VenueDTO>> searchNearbyVenues(BigDecimal longitude, BigDecimal latitude, BigDecimal radius);
    
    /**
     * 搜索热门场馆
     */
    ApiResponse<List<VenueDTO>> getPopularVenues(Integer limit);
    
    /**
     * 更新场馆状态
     */
    ApiResponse<Void> updateVenueStatus(Long id, Venue.VenueStatus status);
    
    /**
     * 更新场馆使用人数
     */
    ApiResponse<Void> updateVenueOccupancy(Long id, Integer occupancy);
    
    /**
     * 更新场馆评分
     */
    ApiResponse<Void> updateVenueRating(Long id, BigDecimal rating);
    
    /**
     * 批量更新场馆状态
     */
    ApiResponse<Void> batchUpdateVenueStatus(List<Long> ids, Venue.VenueStatus status);
    
    /**
     * 统计场馆数据
     */
    ApiResponse<Map<String, Object>> getVenueStatistics(Long merchantId);
    
    /**
     * 统计各类型场馆数量
     */
    ApiResponse<Map<String, Long>> getVenueTypeStatistics(Long merchantId);
    
    /**
     * 统计各子类型场馆数量
     */
    ApiResponse<Map<String, Long>> getVenueSubTypeStatistics(Long merchantId);
    
    /**
     * 检查场馆名称是否重复
     */
    ApiResponse<Boolean> checkVenueNameExists(String name, Long merchantId);
    
    /**
     * 获取场馆类型枚举
     */
    ApiResponse<List<Map<String, String>>> getVenueTypes();
    
    /**
     * 获取场馆子类型枚举
     */
    ApiResponse<List<Map<String, String>>> getVenueSubTypes();
    
    /**
     * 获取场馆状态枚举
     */
    ApiResponse<List<Map<String, String>>> getVenueStatuses();
    
    /**
     * 导入场馆数据
     */
    ApiResponse<Void> importVenues(List<Venue> venues);
    
    /**
     * 导出场馆数据
     */
    ApiResponse<List<Venue>> exportVenues(VenueQueryDTO queryDTO);
    
    /**
     * 场馆数据同步
     */
    ApiResponse<Void> syncVenueData(Long venueId);
    
    /**
     * 场馆数据备份
     */
    ApiResponse<Void> backupVenueData(Long venueId);
    
    /**
     * 场馆数据恢复
     */
    ApiResponse<Void> restoreVenueData(Long venueId);
} 