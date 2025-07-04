package com.sportvenue.social.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.social.service.SocialService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 社交服务控制器
 */
@RestController
@RequestMapping("/social")
public class SocialController {

    @Autowired
    private SocialService socialService;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("社交服务运行正常");
    }

    @GetMapping("/feed")
    public ApiResponse<String> getSocialFeed() {
        String feed = socialService.getSocialFeed();
        return ApiResponse.success(feed);
    }
} 