package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 */
@Tag(name = "健康检查", description = "服务健康状态检查接口")
@Slf4j
@RestController
@RequestMapping("/health")
@CrossOrigin(origins = "*")
public class HealthController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 基础健康检查
     */
    @Operation(summary = "基础健康检查", description = "检查服务是否正常运行")
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "venue-service");
        result.put("version", "1.0.0");
        
        log.info("健康检查请求");
        return ApiResponse.success(result);
    }

    /**
     * 详细健康检查
     */
    @Operation(summary = "详细健康检查", description = "检查数据库、Redis等依赖服务状态")
    @GetMapping("/detailed")
    public ApiResponse<Map<String, Object>> detailedHealth() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "UP");
        result.put("timestamp", LocalDateTime.now());
        result.put("service", "venue-service");
        result.put("version", "1.0.0");
        
        Map<String, Object> components = new HashMap<>();
        
        // 检查数据库连接
        try {
            String dbStatus = jdbcTemplate.queryForObject("SELECT 1", String.class);
            components.put("database", Map.of("status", "UP", "details", "数据库连接正常"));
        } catch (Exception e) {
            components.put("database", Map.of("status", "DOWN", "details", "数据库连接失败: " + e.getMessage()));
            result.put("status", "DOWN");
        }
        
        // 检查Redis连接
        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String redisStatus = (String) redisTemplate.opsForValue().get("health_check");
            components.put("redis", Map.of("status", "UP", "details", "Redis连接正常"));
        } catch (Exception e) {
            components.put("redis", Map.of("status", "DOWN", "details", "Redis连接失败: " + e.getMessage()));
            result.put("status", "DOWN");
        }
        
        result.put("components", components);
        
        log.info("详细健康检查请求，状态：{}", result.get("status"));
        return ApiResponse.success(result);
    }

    /**
     * 数据库健康检查
     */
    @Operation(summary = "数据库健康检查", description = "检查数据库连接状态")
    @GetMapping("/database")
    public ApiResponse<Map<String, Object>> databaseHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String status = jdbcTemplate.queryForObject("SELECT 1", String.class);
            result.put("status", "UP");
            result.put("details", "数据库连接正常");
            result.put("timestamp", LocalDateTime.now());
            
            log.info("数据库健康检查：正常");
            return ApiResponse.success(result);
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("details", "数据库连接失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            
            log.error("数据库健康检查：失败 - {}", e.getMessage());
            return ApiResponse.error("数据库连接失败: " + e.getMessage());
        }
    }

    /**
     * Redis健康检查
     */
    @Operation(summary = "Redis健康检查", description = "检查Redis连接状态")
    @GetMapping("/redis")
    public ApiResponse<Map<String, Object>> redisHealth() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            redisTemplate.opsForValue().set("health_check", "ok");
            String status = (String) redisTemplate.opsForValue().get("health_check");
            
            if ("ok".equals(status)) {
                result.put("status", "UP");
                result.put("details", "Redis连接正常");
                result.put("timestamp", LocalDateTime.now());
                
                log.info("Redis健康检查：正常");
                return ApiResponse.success(result);
            } else {
                result.put("status", "DOWN");
                result.put("details", "Redis读写测试失败");
                result.put("timestamp", LocalDateTime.now());
                
                log.error("Redis健康检查：读写测试失败");
                return ApiResponse.error("Redis读写测试失败");
            }
        } catch (Exception e) {
            result.put("status", "DOWN");
            result.put("details", "Redis连接失败: " + e.getMessage());
            result.put("timestamp", LocalDateTime.now());
            
            log.error("Redis健康检查：失败 - {}", e.getMessage());
            return ApiResponse.error("Redis连接失败: " + e.getMessage());
        }
    }
} 