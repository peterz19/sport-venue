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
 * 场馆管理控制器
 * 提供场馆的CRUD操作和查询功能
 */
@Slf4j
@RestController
@RequestMapping("/venues")
public class VenueController {

    @Autowired
    private VenueService venueService;

    /**
     * 创建场馆
     */
    @PostMapping
    public ApiResponse<VenueDTO> createVenue(@RequestBody Venue venue) {
        log.info("创建场馆请求：{}", venue.getName());
        return venueService.createVenue(venue);
    }

    /**
     * 更新场馆
     */
    @PutMapping("/{id}")
    public ApiResponse<VenueDTO> updateVenue(@PathVariable Long id, @RequestBody Venue venue) {
        log.info("更新场馆请求，ID：{}", id);
        return venueService.updateVenue(id, venue);
    }

    /**
     * 删除场馆
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVenue(@PathVariable Long id) {
        log.info("删除场馆请求，ID：{}", id);
        return venueService.deleteVenue(id);
    }

    /**
     * 根据ID获取场馆详情
     */
    @GetMapping("/{id}")
    public ApiResponse<VenueDTO> getVenueById(@PathVariable Long id) {
        log.info("获取场馆详情请求，ID：{}", id);
        return venueService.getVenueById(id);
    }

    /**
     * 分页查询场馆列表
     */
    @GetMapping
    public ApiResponse<Page<VenueDTO>> getVenueList(VenueQueryDTO queryDTO) {
        log.info("查询场馆列表请求：{}", queryDTO);
        return venueService.getVenueList(queryDTO);
    }

    /**
     * 根据商户ID查询场馆
     */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<VenueDTO>> getVenuesByMerchant(@PathVariable Long merchantId) {
        log.info("查询商户场馆请求，商户ID：{}", merchantId);
        return venueService.getVenuesByMerchant(merchantId);
    }

    /**
     * 根据场馆类型查询
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<VenueDTO>> getVenuesByType(@PathVariable String type) {
        log.info("查询场馆类型请求，类型：{}", type);
        return venueService.getVenuesByType(Venue.VenueType.valueOf(type.toUpperCase()));
    }

    /**
     * 根据场馆子类型查询
     */
    @GetMapping("/subtype/{subType}")
    public ApiResponse<List<VenueDTO>> getVenuesBySubType(@PathVariable String subType) {
        log.info("查询场馆子类型请求，子类型：{}", subType);
        return venueService.getVenuesBySubType(Venue.VenueSubType.valueOf(subType.toUpperCase()));
    }

    /**
     * 搜索附近场馆
     */
    @GetMapping("/nearby")
    public ApiResponse<List<VenueDTO>> searchNearbyVenues(
            @RequestParam BigDecimal longitude,
            @RequestParam BigDecimal latitude,
            @RequestParam(defaultValue = "0.01") BigDecimal radius) {
        log.info("搜索附近场馆请求，经度：{}，纬度：{}，半径：{}", longitude, latitude, radius);
        return venueService.searchNearbyVenues(longitude, latitude, radius);
    }

    /**
     * 获取热门场馆
     */
    @GetMapping("/popular")
    public ApiResponse<List<VenueDTO>> getPopularVenues(@RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门场馆请求，限制：{}", limit);
        return venueService.getPopularVenues(limit);
    }

    /**
     * 更新场馆状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateVenueStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新场馆状态请求，ID：{}，状态：{}", id, status);
        return venueService.updateVenueStatus(id, Venue.VenueStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 更新场馆使用人数
     */
    @PutMapping("/{id}/occupancy")
    public ApiResponse<Void> updateVenueOccupancy(@PathVariable Long id, @RequestParam Integer occupancy) {
        log.info("更新场馆使用人数请求，ID：{}，人数：{}", id, occupancy);
        return venueService.updateVenueOccupancy(id, occupancy);
    }

    /**
     * 更新场馆评分
     */
    @PutMapping("/{id}/rating")
    public ApiResponse<Void> updateVenueRating(@PathVariable Long id, @RequestParam BigDecimal rating) {
        log.info("更新场馆评分请求，ID：{}，评分：{}", id, rating);
        return venueService.updateVenueRating(id, rating);
    }

    /**
     * 获取场馆类型列表
     */
    @GetMapping("/types")
    public ApiResponse<List<Map<String, String>>> getVenueTypes() {
        log.info("获取场馆类型列表请求");
        return venueService.getVenueTypes();
    }

    /**
     * 获取场馆子类型列表
     */
    @GetMapping("/subtypes")
    public ApiResponse<List<Map<String, String>>> getVenueSubTypes() {
        log.info("获取场馆子类型列表请求");
        return venueService.getVenueSubTypes();
    }

    /**
     * 获取场馆状态列表
     */
    @GetMapping("/statuses")
    public ApiResponse<List<Map<String, String>>> getVenueStatuses() {
        log.info("获取场馆状态列表请求");
        return venueService.getVenueStatuses();
    }
} 