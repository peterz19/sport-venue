package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.VenueDTO;
import com.sportvenue.venue.dto.VenueQueryDTO;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.service.VenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * B端商户场馆控制器
 * 提供B端商户管理场馆、查看统计等功能的API
 */
@Slf4j
@RestController
@RequestMapping("/business/venues")
public class BusinessVenueController {

    @Autowired
    private VenueService venueService;

    /**
     * 创建场馆（B端商户）
     */
    @PostMapping
    public ApiResponse<VenueDTO> createVenue(@RequestBody Venue venue) {
        log.info("B端商户创建场馆请求：{}", venue.getName());
        return venueService.createVenue(venue);
    }

    /**
     * 更新场馆（B端商户）
     */
    @PutMapping("/{id}")
    public ApiResponse<VenueDTO> updateVenue(@PathVariable Long id, @RequestBody Venue venue) {
        log.info("B端商户更新场馆请求，ID：{}", id);
        return venueService.updateVenue(id, venue);
    }

    /**
     * 删除场馆（B端商户）
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVenue(@PathVariable Long id) {
        log.info("B端商户删除场馆请求，ID：{}", id);
        return venueService.deleteVenue(id);
    }

    /**
     * 根据ID获取场馆详情（B端商户）
     */
    @GetMapping("/{id}")
    public ApiResponse<VenueDTO> getVenueById(@PathVariable Long id) {
        log.info("B端商户获取场馆详情请求，ID：{}", id);
        return venueService.getVenueById(id);
    }

    /**
     * 分页查询场馆列表（B端商户）
     */
    @GetMapping
    public ApiResponse<Page<VenueDTO>> getVenueList(VenueQueryDTO queryDTO) {
        log.info("B端商户查询场馆列表请求：{}", queryDTO);
        return venueService.getVenueList(queryDTO);
    }

    /**
     * 根据商户ID查询场馆（B端商户）
     */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<VenueDTO>> getVenuesByMerchant(@PathVariable Long merchantId) {
        log.info("B端商户查询商户场馆请求，商户ID：{}", merchantId);
        return venueService.getVenuesByMerchant(merchantId);
    }

    /**
     * 更新场馆状态（B端商户）
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateVenueStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("B端商户更新场馆状态请求，ID：{}，状态：{}", id, status);
        return venueService.updateVenueStatus(id, Venue.VenueStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 更新场馆使用人数（B端商户）
     */
    @PutMapping("/{id}/occupancy")
    public ApiResponse<Void> updateVenueOccupancy(@PathVariable Long id, @RequestParam Integer occupancy) {
        log.info("B端商户更新场馆使用人数请求，ID：{}，人数：{}", id, occupancy);
        return venueService.updateVenueOccupancy(id, occupancy);
    }

    /**
     * 更新场馆评分（B端商户）
     */
    @PutMapping("/{id}/rating")
    public ApiResponse<Void> updateVenueRating(@PathVariable Long id, @RequestParam BigDecimal rating) {
        log.info("B端商户更新场馆评分请求，ID：{}，评分：{}", id, rating);
        return venueService.updateVenueRating(id, rating);
    }

    /**
     * 批量更新场馆状态（B端商户）
     */
    @PutMapping("/batch/status")
    public ApiResponse<Void> batchUpdateVenueStatus(@RequestParam List<Long> ids, @RequestParam String status) {
        log.info("B端商户批量更新场馆状态请求，场馆数量：{}，状态：{}", ids.size(), status);
        return venueService.batchUpdateVenueStatus(ids, Venue.VenueStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 获取场馆统计信息（B端商户）
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getVenueStatistics(@RequestParam Long merchantId) {
        log.info("B端商户获取场馆统计信息请求，商户ID：{}", merchantId);
        return venueService.getVenueStatistics(merchantId);
    }

    /**
     * 获取场馆类型统计（B端商户）
     */
    @GetMapping("/statistics/types")
    public ApiResponse<Map<String, Long>> getVenueTypeStatistics(@RequestParam Long merchantId) {
        log.info("B端商户获取场馆类型统计请求，商户ID：{}", merchantId);
        return venueService.getVenueTypeStatistics(merchantId);
    }

    /**
     * 获取场馆子类型统计（B端商户）
     */
    @GetMapping("/statistics/subtypes")
    public ApiResponse<Map<String, Long>> getVenueSubTypeStatistics(@RequestParam Long merchantId) {
        log.info("B端商户获取场馆子类型统计请求，商户ID：{}", merchantId);
        return venueService.getVenueSubTypeStatistics(merchantId);
    }

    /**
     * 获取场馆实时数据（B端商户）
     */
    @GetMapping("/{id}/realtime")
    public ApiResponse<Map<String, Object>> getVenueRealtimeData(@PathVariable Long id) {
        log.info("B端商户获取场馆实时数据请求，场馆ID：{}", id);
        return venueService.getVenueRealtimeData(id);
    }

    /**
     * 获取场馆预约统计（B端商户）
     */
    @GetMapping("/{id}/reservation-stats")
    public ApiResponse<Map<String, Object>> getVenueReservationStats(@PathVariable Long id) {
        log.info("B端商户获取场馆预约统计请求，场馆ID：{}", id);
        return venueService.getVenueReservationStats(id);
    }

    /**
     * 获取场馆打卡统计（B端商户）
     */
    @GetMapping("/{id}/checkin-stats")
    public ApiResponse<Map<String, Object>> getVenueCheckInStats(@PathVariable Long id) {
        log.info("B端商户获取场馆打卡统计请求，场馆ID：{}", id);
        return venueService.getVenueCheckInStats(id);
    }

    /**
     * 获取场馆收入统计（B端商户）
     */
    @GetMapping("/{id}/revenue-stats")
    public ApiResponse<Map<String, Object>> getVenueRevenueStats(@PathVariable Long id) {
        log.info("B端商户获取场馆收入统计请求，场馆ID：{}", id);
        return venueService.getVenueRevenueStats(id);
    }

    /**
     * 导入场馆数据（B端商户）
     */
    @PostMapping("/import")
    public ApiResponse<Void> importVenues(@RequestBody List<Venue> venues) {
        log.info("B端商户导入场馆数据请求，场馆数量：{}", venues.size());
        return venueService.importVenues(venues);
    }

    /**
     * 导出场馆数据（B端商户）
     */
    @GetMapping("/export")
    public ApiResponse<List<Venue>> exportVenues(VenueQueryDTO queryDTO) {
        log.info("B端商户导出场馆数据请求：{}", queryDTO);
        return venueService.exportVenues(queryDTO);
    }
} 