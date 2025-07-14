package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.service.UserService;
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
 * 用户管理控制器
 * 提供用户管理、权限管理等功能的API
 */
@Tag(name = "用户管理", description = "用户信息管理接口")
@Slf4j
@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @Operation(summary = "创建用户", description = "创建新用户")
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        log.info("创建用户请求，用户名：{}", user.getUsername());
        return userService.createUser(user);
    }

    /**
     * 更新用户
     */
    @Operation(summary = "更新用户", description = "更新用户信息")
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable("id") Long id, @RequestBody User user) {
        log.info("更新用户请求，ID：{}", id);
        return userService.updateUser(id, user);
    }

    /**
     * 删除用户
     */
    @Operation(summary = "删除用户", description = "删除指定用户")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable("id") Long id) {
        log.info("删除用户请求，ID：{}", id);
        return userService.deleteUser(id);
    }

    /**
     * 根据ID获取用户详情
     */
    @Operation(summary = "获取用户详情", description = "根据ID获取用户详细信息")
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable("id") Long id) {
        log.info("获取用户详情请求，ID：{}", id);
        return userService.getUserById(id);
    }

    /**
     * 分页查询用户列表
     */
    @Operation(summary = "分页查询用户列表", description = "支持按用户名、用户类型、状态筛选")
    @GetMapping
    public ApiResponse<Page<User>> getUserList(
            @Parameter(description = "页码，从0开始") @RequestParam(value = "page", defaultValue = "0") Integer page,
            @Parameter(description = "每页大小") @RequestParam(value = "size", defaultValue = "10") Integer size,
            @Parameter(description = "用户名（模糊查询）") @RequestParam(value = "username", required = false) String username,
            @Parameter(description = "用户类型") @RequestParam(value = "userType", required = false) String userType,
            @Parameter(description = "用户状态") @RequestParam(value = "status", required = false) String status) {
        log.info("查询用户列表请求，页码：{}，大小：{}，用户名：{}，用户类型：{}，状态：{}", 
                page, size, username, userType, status);
        return userService.getUserList(page, size, username, userType, status);
    }

    /**
     * 根据商户ID查询用户
     */
    @Operation(summary = "查询商户用户", description = "根据商户ID查询该商户下的所有用户")
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<User>> getUsersByMerchant(@PathVariable("merchantId") Long merchantId) {
        log.info("查询商户用户请求，商户ID：{}", merchantId);
        return userService.getUsersByMerchant(merchantId);
    }

    /**
     * 更新用户状态
     */
    @Operation(summary = "更新用户状态", description = "启用、停用或锁定用户")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable("id") Long id, @RequestParam("status") String status) {
        log.info("更新用户状态请求，ID：{}，状态：{}", id, status);
        return userService.updateUserStatus(id, User.UserStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 重置用户密码
     */
    @Operation(summary = "重置用户密码", description = "重置用户密码为默认密码")
    @PutMapping("/{id}/reset-password")
    public ApiResponse<Void> resetUserPassword(@PathVariable("id") Long id) {
        log.info("重置用户密码请求，ID：{}", id);
        return userService.resetUserPassword(id);
    }

    /**
     * 修改用户密码
     */
    @Operation(summary = "修改用户密码", description = "用户修改自己的密码")
    @PutMapping("/{id}/change-password")
    public ApiResponse<Void> changeUserPassword(@PathVariable("id") Long id,
                                               @RequestParam("oldPassword") String oldPassword,
                                               @RequestParam("newPassword") String newPassword) {
        log.info("修改用户密码请求，ID：{}", id);
        return userService.changeUserPassword(id, oldPassword, newPassword);
    }

    /**
     * 分配用户角色
     */
    @Operation(summary = "分配用户角色", description = "为用户分配角色")
    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignUserRoles(@PathVariable("id") Long id, @RequestParam("roleIds") List<Long> roleIds) {
        log.info("分配用户角色请求，用户ID：{}，角色数量：{}", id, roleIds.size());
        return userService.assignUserRoles(id, roleIds);
    }

    /**
     * 获取用户角色
     */
    @Operation(summary = "获取用户角色", description = "获取用户的所有角色")
    @GetMapping("/{id}/roles")
    public ApiResponse<List<Map<String, Object>>> getUserRoles(@PathVariable("id") Long id) {
        log.info("获取用户角色请求，ID：{}", id);
        return userService.getUserRoles(id);
    }

    /**
     * 获取用户权限
     */
    @Operation(summary = "获取用户权限", description = "获取用户的所有权限")
    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions(@PathVariable("id") Long id) {
        log.info("获取用户权限请求，ID：{}", id);
        return userService.getUserPermissions(id);
    }

    /**
     * 用户登录
     */
    @Operation(summary = "用户登录", description = "用户名密码登录")
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        log.info("用户登录请求，用户名：{}", username);
        return userService.login(username, password);
    }

    /**
     * 用户登出
     */
    @Operation(summary = "用户登出", description = "用户登出")
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestParam String token) {
        log.info("用户登出请求");
        return userService.logout(token);
    }

    /**
     * 获取当前用户信息
     */
    @Operation(summary = "获取当前用户信息", description = "获取当前登录用户信息")
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(@RequestParam String token) {
        log.info("获取当前用户信息请求");
        return userService.getCurrentUser(token);
    }

    /**
     * 获取用户统计信息
     */
    @Operation(summary = "获取用户统计信息", description = "获取用户相关的统计数据")
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        log.info("获取用户统计信息请求");
        return userService.getUserStatistics();
    }

    /**
     * 获取用户类型统计
     */
    @Operation(summary = "获取用户类型统计", description = "按用户类型统计用户数量")
    @GetMapping("/statistics/types")
    public ApiResponse<Map<String, Long>> getUserTypeStatistics() {
        log.info("获取用户类型统计请求");
        return userService.getUserTypeStatistics();
    }

    /**
     * 批量导入用户
     */
    @Operation(summary = "批量导入用户", description = "批量导入用户数据")
    @PostMapping("/import")
    public ApiResponse<Void> importUsers(@RequestBody List<User> users) {
        log.info("批量导入用户请求，用户数量：{}", users.size());
        return userService.importUsers(users);
    }

    /**
     * 导出用户数据
     */
    @Operation(summary = "导出用户数据", description = "导出用户数据")
    @GetMapping("/export")
    public ApiResponse<List<User>> exportUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status) {
        log.info("导出用户数据请求，用户类型：{}，状态：{}", userType, status);
        return userService.exportUsers(userType, status);
    }
} 