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

import java.util.List;
import java.util.Map;

/**
 * 场馆管理控制器
 * 提供场馆管理相关功能的API
 */
@Tag(name = "场馆管理", description = "场馆信息管理接口")
@Slf4j
@RestController
@RequestMapping("/venues")
@CrossOrigin(origins = "*")
public class VenueController {

    @Autowired
    private VenueService venueService;

    /**
     * 创建场馆
     */
    @Operation(summary = "创建场馆", description = "创建新场馆")
    @PostMapping
    public ApiResponse<VenueDTO> createVenue(@RequestBody Venue venue) {
        log.info("创建场馆请求，场馆名称：{}", venue.getName());
        return venueService.createVenue(venue);
    }

    /**
     * 更新场馆
     */
    @Operation(summary = "更新场馆", description = "更新场馆信息")
    @PutMapping("/{id}")
    public ApiResponse<VenueDTO> updateVenue(@PathVariable("id") Long id, @RequestBody Venue venue) {
        log.info("更新场馆请求，ID：{}", id);
        return venueService.updateVenue(id, venue);
    }

    /**
     * 删除场馆
     */
    @Operation(summary = "删除场馆", description = "删除指定场馆")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteVenue(@PathVariable("id") Long id) {
        log.info("删除场馆请求，ID：{}", id);
        return venueService.deleteVenue(id);
    }

    /**
     * 根据ID获取场馆详情
     */
    @Operation(summary = "获取场馆详情", description = "根据ID获取场馆详细信息")
    @GetMapping("/{id}")
    public ApiResponse<VenueDTO> getVenueById(@PathVariable("id") Long id) {
        log.info("获取场馆详情请求，ID：{}", id);
        return venueService.getVenueById(id);
    }

    /**
     * 分页查询场馆列表
     */
    @Operation(summary = "分页查询场馆列表", description = "支持按名称、类型、状态、商户ID筛选")
    @GetMapping
    public ApiResponse<Page<VenueDTO>> getVenueList(
            @Parameter(description = "页码，从0开始") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "场馆名称（模糊查询）") @RequestParam(value = "name", required = false) String name,
            @Parameter(description = "场馆类型") @RequestParam(value = "type", required = false) String type,
            @Parameter(description = "场馆状态") @RequestParam(value = "status", required = false) String status,
            @Parameter(description = "商户ID") @RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("查询场馆列表请求，页码：{}，大小：{}，名称：{}，类型：{}，状态：{}，商户ID：{}", 
                page, size, name, type, status, merchantId);
        
        VenueQueryDTO queryDTO = new VenueQueryDTO();
        queryDTO.setPage(page);
        queryDTO.setSize(size);
        queryDTO.setName(name);
        if (type != null && !type.trim().isEmpty()) {
            try {
                queryDTO.setType(Venue.VenueType.valueOf(type.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的场馆类型参数：{}", type);
            }
        }
        if (status != null && !status.trim().isEmpty()) {
            try {
                queryDTO.setStatus(Venue.VenueStatus.valueOf(status.toUpperCase()));
            } catch (IllegalArgumentException e) {
                log.warn("无效的场馆状态参数：{}", status);
            }
        }
        queryDTO.setMerchantId(merchantId);
        
        return venueService.getVenueList(queryDTO);
    }

    /**
     * 根据商户ID查询场馆
     */
    @Operation(summary = "查询商户场馆", description = "根据商户ID查询该商户下的所有场馆")
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<VenueDTO>> getVenuesByMerchant(@PathVariable("merchantId") Long merchantId) {
        log.info("查询商户场馆请求，商户ID：{}", merchantId);
        return venueService.getVenuesByMerchant(merchantId);
    }

    /**
     * 更新场馆状态
     */
    @Operation(summary = "更新场馆状态", description = "启用、停用或维护场馆")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateVenueStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        log.info("更新场馆状态请求，ID：{}，状态：{}", id, status);
        return venueService.updateVenueStatus(id, Venue.VenueStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 获取场馆统计信息
     */
    @Operation(summary = "获取场馆统计信息", description = "获取场馆相关的统计数据")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getVenueStatistics(@RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("获取场馆统计信息请求，商户ID：{}", merchantId);
        return venueService.getVenueStatistics(merchantId);
    }

    /**
     * 获取场馆类型统计
     */
    @Operation(summary = "获取场馆类型统计", description = "按场馆类型统计场馆数量")
    @GetMapping("/statistics/types")
    public ApiResponse<Map<String, Long>> getVenueTypeStatistics(@RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("获取场馆类型统计请求，商户ID：{}", merchantId);
        return venueService.getVenueTypeStatistics(merchantId);
    }

    /**
     * 批量导入场馆
     */
    @Operation(summary = "批量导入场馆", description = "批量导入场馆数据")
    @PostMapping("/import")
    public ApiResponse<Void> importVenues(@RequestBody List<Venue> venues) {
        log.info("批量导入场馆请求，场馆数量：{}", venues.size());
        return venueService.importVenues(venues);
    }

    /**
     * 导出场馆数据
     */
    @Operation(summary = "导出场馆数据", description = "导出场馆数据")
    @GetMapping("/export")
    public ApiResponse<List<Venue>> exportVenues(
            @RequestParam(value = "type", required = false) String type,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        log.info("导出场馆数据请求，类型：{}，状态：{}，商户ID：{}", type, status, merchantId);
        
        VenueQueryDTO queryDTO = new VenueQueryDTO();
        if (type != null) {
            queryDTO.setType(Venue.VenueType.valueOf(type.toUpperCase()));
        }
        if (status != null) {
            queryDTO.setStatus(Venue.VenueStatus.valueOf(status.toUpperCase()));
        }
        queryDTO.setMerchantId(merchantId);
        
        return venueService.exportVenues(queryDTO);
    }

    /**
     * 更新场馆使用人数
     */
    @Operation(summary = "更新场馆使用人数", description = "更新场馆的占用状态")
    @PutMapping("/{id}/occupancy")
    public ApiResponse<Void> updateVenueOccupancy(@PathVariable("id") Long id, @RequestParam("occupancy") Integer occupancy) {
        log.info("更新场馆使用人数请求，ID：{}，人数：{}", id, occupancy);
        return venueService.updateVenueOccupancy(id, occupancy);
    }

    /**
     * 获取场馆状态列表
     */
    @Operation(summary = "获取场馆状态列表", description = "获取所有可用的场馆状态")
    @GetMapping("/statuses")
    public ApiResponse<List<Map<String, String>>> getVenueStatuses() {
        log.info("获取场馆状态列表请求");
        return venueService.getVenueStatuses();
    }

    /**
     * 获取场馆收费类型列表
     */
    @Operation(summary = "获取场馆收费类型列表", description = "获取所有可用的场馆收费类型")
    @GetMapping("/charge-types")
    public ApiResponse<List<Map<String, String>>> getVenueChargeTypes() {
        log.info("获取场馆收费类型列表请求");
        return venueService.getVenueChargeTypes();
    }

    /**
     * 获取场馆空间类型列表
     */
    @Operation(summary = "获取场馆空间类型列表", description = "获取所有可用的场馆空间类型")
    @GetMapping("/space-types")
    public ApiResponse<List<Map<String, String>>> getVenueSpaceTypes() {
        log.info("获取场馆空间类型列表请求");
        return venueService.getVenueSpaceTypes();
    }

    /**
     * 获取场馆类型列表
     */
    @Operation(summary = "获取场馆类型列表", description = "获取所有可用的场馆类型")
    @GetMapping("/types")
    public ApiResponse<List<Map<String, String>>> getVenueTypes() {
        log.info("获取场馆类型列表请求");
        return venueService.getVenueTypes();
    }
} 