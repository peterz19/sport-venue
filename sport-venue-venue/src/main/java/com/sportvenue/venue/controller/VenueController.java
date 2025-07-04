package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.service.VenueService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 场馆服务控制器
 */
@RestController
@RequestMapping("/venue")
public class VenueController {

    @Autowired
    private VenueService venueService;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("场馆服务运行正常");
    }

    @GetMapping("/{venueId}")
    public ApiResponse<String> getVenueInfo(@PathVariable Long venueId) {
        String venueInfo = venueService.getVenueInfo(venueId);
        return ApiResponse.success(venueInfo);
    }

    @GetMapping("/{venueId}/occupancy")
    public ApiResponse<Integer> getCurrentOccupancy(@PathVariable Long venueId) {
        int occupancy = venueService.getCurrentOccupancy(venueId);
        return ApiResponse.success(occupancy);
    }
} 