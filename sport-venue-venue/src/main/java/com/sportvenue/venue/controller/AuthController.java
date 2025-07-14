package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.config.JwtConfig;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.UserRepository;
import com.sportvenue.venue.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证控制器
 * 提供登录、登出、获取用户信息等认证相关功能
 */
@Tag(name = "认证管理", description = "用户登录、登出、认证接口")
@Slf4j
@RestController
@RequestMapping("/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录，返回token和用户信息")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestBody Map<String, String> loginRequest) {
        String username = loginRequest.get("username");
        String password = loginRequest.get("password");
        
        log.info("用户登录请求，用户名：{}", username);
        
        try {
            // 调用用户服务进行登录验证
            ApiResponse<Map<String, Object>> loginResult = userService.login(username, password);
            
            if (loginResult.getCode() == 200) {
                Map<String, Object> data = loginResult.getData();
                User user = (User) data.get("user");
                
                // 生成JWT token
                String token = jwtConfig.generateToken(user.getUsername(), user.getId(), user.getUserType().name());
                
                // 构建返回数据
                Map<String, Object> result = new HashMap<>();
                result.put("token", token);
                result.put("user", user);
                result.put("expiresIn", 86400); // 24小时
                
                log.info("用户登录成功，用户名：{}，用户类型：{}", username, user.getUserType());
                return ApiResponse.success(result);
            } else {
                return loginResult;
            }
        } catch (Exception e) {
            log.error("登录失败：{}", e.getMessage(), e);
            return ApiResponse.error("登录失败：" + e.getMessage());
        }
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "清除用户登录状态")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestHeader("Authorization") String token) {
        log.info("用户登出请求");
        
        try {
            // 清除token（这里可以实现token黑名单机制）
            if (token != null && token.startsWith("Bearer ")) {
                String actualToken = token.substring(7);
                // TODO: 将token加入黑名单
                log.info("Token已加入黑名单：{}", actualToken);
            }
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("登出失败：{}", e.getMessage(), e);
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "根据token获取当前登录用户信息")
    @GetMapping("/user/info")
    public ApiResponse<User> getCurrentUser(@RequestHeader("Authorization") String token) {
        log.info("获取当前用户信息请求");
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResponse.error("无效的token");
            }
            
            String actualToken = token.substring(7);
            
            // 从token中获取用户信息
            String username = jwtConfig.getUsernameFromToken(actualToken);
            Long userId = jwtConfig.getUserIdFromToken(actualToken);
            
            if (username == null || userId == null) {
                return ApiResponse.error("token解析失败");
            }
            
            // 验证token是否过期
            if (jwtConfig.isTokenExpired(actualToken)) {
                return ApiResponse.error("token已过期");
            }
            
            // 获取用户详细信息
            ApiResponse<User> userResult = userService.getUserById(userId);
            if (userResult.getCode() == 200) {
                User user = userResult.getData();
                // 清除敏感信息
                user.setPassword(null);
                return ApiResponse.success(user);
            } else {
                return userResult;
            }
        } catch (Exception e) {
            log.error("获取用户信息失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户信息失败：" + e.getMessage());
        }
    }

    /**
     * 刷新token
     */
    @Operation(summary = "刷新token", description = "刷新用户token")
    @PostMapping("/refresh")
    public ApiResponse<Map<String, Object>> refreshToken(@RequestHeader("Authorization") String token) {
        log.info("刷新token请求");
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResponse.error("无效的token");
            }
            
            String actualToken = token.substring(7);
            
            // 验证原token
            String username = jwtConfig.getUsernameFromToken(actualToken);
            Long userId = jwtConfig.getUserIdFromToken(actualToken);
            String userType = jwtConfig.getUserTypeFromToken(actualToken);
            
            if (username == null || userId == null || userType == null) {
                return ApiResponse.error("token解析失败");
            }
            
            if (jwtConfig.isTokenExpired(actualToken)) {
                return ApiResponse.error("token已过期");
            }
            
            // 生成新token
            String newToken = jwtConfig.generateToken(username, userId, userType);
            
            Map<String, Object> result = new HashMap<>();
            result.put("token", newToken);
            result.put("expiresIn", 86400);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("刷新token失败：{}", e.getMessage(), e);
            return ApiResponse.error("刷新token失败：" + e.getMessage());
        }
    }

    /**
     * 验证token
     */
    @Operation(summary = "验证token", description = "验证token是否有效")
    @PostMapping("/verify")
    public ApiResponse<Map<String, Object>> verifyToken(@RequestHeader("Authorization") String token) {
        log.info("验证token请求");
        
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                return ApiResponse.error("无效的token");
            }
            
            String actualToken = token.substring(7);
            
            // 验证token
            String username = jwtConfig.getUsernameFromToken(actualToken);
            Long userId = jwtConfig.getUserIdFromToken(actualToken);
            String userType = jwtConfig.getUserTypeFromToken(actualToken);
            
            if (username == null || userId == null || userType == null) {
                return ApiResponse.error("token解析失败");
            }
            
            if (jwtConfig.isTokenExpired(actualToken)) {
                return ApiResponse.error("token已过期");
            }
            
            Map<String, Object> result = new HashMap<>();
            result.put("valid", true);
            result.put("username", username);
            result.put("userId", userId);
            result.put("userType", userType);
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("验证token失败：{}", e.getMessage(), e);
            return ApiResponse.error("验证token失败：" + e.getMessage());
        }
    }

    /**
     * 开发专用：重置admin密码
     */
    @Operation(summary = "重置admin密码", description = "开发专用，重置admin用户密码为123456")
    @PostMapping("/dev/reset-admin-password")
    public ApiResponse<Void> resetAdminPassword() {
        log.info("重置admin密码请求");
        
        try {
            User adminUser = userRepository.findByUsername("admin").orElse(null);
            if (adminUser == null) {
                return ApiResponse.error("admin用户不存在");
            }
            
            // 重置密码为123456
            adminUser.setPassword(passwordEncoder.encode("123456"));
            adminUser.setUpdateTime(java.time.LocalDateTime.now());
            userRepository.save(adminUser);
            
            log.info("admin密码重置成功");
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("重置admin密码失败：{}", e.getMessage(), e);
            return ApiResponse.error("重置admin密码失败：" + e.getMessage());
        }
    }
} 