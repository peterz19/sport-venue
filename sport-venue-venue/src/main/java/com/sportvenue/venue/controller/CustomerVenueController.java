package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.VenueDTO;
import com.sportvenue.venue.dto.VenueQueryDTO;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.service.VenueService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * C端用户场馆控制器
 * 提供C端用户查询场馆、预约等功能的API
 */
@Tag(name = "C端场馆查询", description = "C端用户场馆查询接口")
@Slf4j
@RestController
@RequestMapping("/customer/venues")
public class CustomerVenueController {

    @Autowired
    private VenueService venueService;

    /**
     * 分页查询场馆列表（C端用户）
     */
    @GetMapping
    public ApiResponse<Page<VenueDTO>> getVenueList(VenueQueryDTO queryDTO) {
        log.info("C端用户查询场馆列表请求：{}", queryDTO);
        // C端用户只能查看活跃状态的场馆
        queryDTO.setStatus(Venue.VenueStatus.ACTIVE);
        return venueService.getVenueList(queryDTO);
    }

    /**
     * 根据ID获取场馆详情（C端用户）
     */
    @GetMapping("/{id}")
    public ApiResponse<VenueDTO> getVenueById(@PathVariable("id") Long id) {
        log.info("C端用户获取场馆详情请求，ID：{}", id);
        return venueService.getVenueById(id);
    }

    /**
     * 根据场馆类型查询（C端用户）
     */
    @GetMapping("/type/{type}")
    public ApiResponse<List<VenueDTO>> getVenuesByType(@PathVariable("type") String type) {
        log.info("C端用户查询场馆类型请求，类型：{}", type);
        return venueService.getVenuesByType(Venue.VenueType.valueOf(type.toUpperCase()));
    }

    /**
     * 搜索附近场馆（C端用户）
     */
    @GetMapping("/nearby")
    public ApiResponse<List<VenueDTO>> searchNearbyVenues(
            @RequestParam("longitude") BigDecimal longitude,
            @RequestParam("latitude") BigDecimal latitude,
            @RequestParam(value = "radius", defaultValue = "0.01") BigDecimal radius) {
        log.info("C端用户搜索附近场馆请求，经度：{}，纬度：{}，半径：{}", longitude, latitude, radius);
        return venueService.searchNearbyVenues(longitude, latitude, radius);
    }

    /**
     * 获取热门场馆（C端用户）
     */
    @GetMapping("/popular")
    public ApiResponse<List<VenueDTO>> getPopularVenues(@RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("C端用户获取热门场馆请求，限制：{}", limit);
        return venueService.getPopularVenues(limit);
    }

    /**
     * 根据场馆名称搜索（C端用户）
     */
    @GetMapping("/search")
    public ApiResponse<List<VenueDTO>> searchVenuesByName(@RequestParam("name") String name) {
        log.info("C端用户搜索场馆请求，名称：{}", name);
        return venueService.searchVenuesByName(name);
    }

    /**
     * 获取场馆类型列表（C端用户）
     */
    @GetMapping("/types")
    public ApiResponse<List<Map<String, String>>> getVenueTypes() {
        log.info("C端用户获取场馆类型列表请求");
        return venueService.getVenueTypes();
    }

    /**
     * 获取推荐场馆（基于用户位置和偏好）
     */
    @GetMapping("/recommended")
    public ApiResponse<List<VenueDTO>> getRecommendedVenues(
            @RequestParam(value = "longitude", required = false) BigDecimal longitude,
            @RequestParam(value = "latitude", required = false) BigDecimal latitude,
            @RequestParam(value = "limit", defaultValue = "10") Integer limit) {
        log.info("C端用户获取推荐场馆请求，经度：{}，纬度：{}，限制：{}", longitude, latitude, limit);
        return venueService.getRecommendedVenues(longitude, latitude, limit);
    }

    /**
     * 获取场馆实时信息（在线人数、可用性等）
     */
    @GetMapping("/{id}/realtime")
    public ApiResponse<Map<String, Object>> getVenueRealtimeInfo(@PathVariable("id") Long id) {
        log.info("C端用户获取场馆实时信息请求，场馆ID：{}", id);
        return venueService.getVenueRealtimeInfo(id);
    }
} 