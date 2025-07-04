package com.sportvenue.prediction.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.prediction.service.PredictionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 预测服务控制器
 */
@RestController
@RequestMapping("/prediction")
public class PredictionController {

    @Autowired
    private PredictionService predictionService;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("预测服务运行正常");
    }

    @GetMapping("/occupancy/{venueId}")
    public ApiResponse<Integer> predictOccupancy(@PathVariable Long venueId) {
        int predictedOccupancy = predictionService.predictOccupancy(venueId);
        return ApiResponse.success(predictedOccupancy);
    }
} 