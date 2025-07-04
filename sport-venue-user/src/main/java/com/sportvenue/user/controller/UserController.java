package com.sportvenue.user.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户服务控制器
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("用户服务运行正常");
    }

    @GetMapping("/{userId}")
    public ApiResponse<String> getUserInfo(@PathVariable Long userId) {
        String userInfo = userService.getUserInfo(userId);
        return ApiResponse.success(userInfo);
    }
} 