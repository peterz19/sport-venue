package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 场馆服务健康检查控制器
 */
@Tag(name = "健康检查", description = "服务健康检查接口")
@RestController
@RequestMapping("/health")
public class HealthController {

    @Operation(summary = "健康检查", description = "检查场馆服务运行状态")
    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("场馆服务运行正常");
    }
} 