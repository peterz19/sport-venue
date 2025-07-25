package com.sportvenue.user.controller;

import com.sportvenue.common.model.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户服务健康检查控制器
 */
@RestController
@RequestMapping("/health")
public class HealthController {

    @GetMapping
    public ApiResponse<String> health() {
        return ApiResponse.success("用户服务运行正常");
    }
} 