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
@Tag(name = "用户管理", description = "用户注册、登录、管理接口")
@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 创建用户
     */
    @PostMapping
    public ApiResponse<User> createUser(@RequestBody User user) {
        log.info("创建用户请求：{}", user.getUsername());
        return userService.createUser(user);
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        log.info("更新用户请求，ID：{}", id);
        return userService.updateUser(id, user);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        log.info("删除用户请求，ID：{}", id);
        return userService.deleteUser(id);
    }

    /**
     * 根据ID获取用户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        log.info("获取用户详情请求，ID：{}", id);
        return userService.getUserById(id);
    }

    /**
     * 分页查询用户列表
     */
    @GetMapping
    public ApiResponse<Page<User>> getUserList(
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status) {
        log.info("查询用户列表请求，页码：{}，大小：{}", page, size);
        return userService.getUserList(page, size, username, userType, status);
    }

    /**
     * 根据商户ID查询用户
     */
    @GetMapping("/merchant/{merchantId}")
    public ApiResponse<List<User>> getUsersByMerchant(@PathVariable Long merchantId) {
        log.info("查询商户用户请求，商户ID：{}", merchantId);
        return userService.getUsersByMerchant(merchantId);
    }

    /**
     * 更新用户状态
     */
    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateUserStatus(@PathVariable Long id, @RequestParam String status) {
        log.info("更新用户状态请求，ID：{}，状态：{}", id, status);
        return userService.updateUserStatus(id, User.UserStatus.valueOf(status.toUpperCase()));
    }

    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/password/reset")
    public ApiResponse<Void> resetUserPassword(@PathVariable Long id) {
        log.info("重置用户密码请求，ID：{}", id);
        return userService.resetUserPassword(id);
    }

    /**
     * 修改用户密码
     */
    @PutMapping("/{id}/password/change")
    public ApiResponse<Void> changeUserPassword(@PathVariable Long id, 
                                               @RequestParam String oldPassword,
                                               @RequestParam String newPassword) {
        log.info("修改用户密码请求，ID：{}", id);
        return userService.changeUserPassword(id, oldPassword, newPassword);
    }

    /**
     * 分配用户角色
     */
    @PutMapping("/{id}/roles")
    public ApiResponse<Void> assignUserRoles(@PathVariable Long id, @RequestParam List<Long> roleIds) {
        log.info("分配用户角色请求，用户ID：{}，角色数量：{}", id, roleIds.size());
        return userService.assignUserRoles(id, roleIds);
    }

    /**
     * 获取用户角色
     */
    @GetMapping("/{id}/roles")
    public ApiResponse<List<Map<String, Object>>> getUserRoles(@PathVariable Long id) {
        log.info("获取用户角色请求，ID：{}", id);
        return userService.getUserRoles(id);
    }

    /**
     * 获取用户权限
     */
    @GetMapping("/{id}/permissions")
    public ApiResponse<List<Map<String, Object>>> getUserPermissions(@PathVariable Long id) {
        log.info("获取用户权限请求，ID：{}", id);
        return userService.getUserPermissions(id);
    }

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@RequestParam String username, @RequestParam String password) {
        log.info("用户登录请求，用户名：{}", username);
        return userService.login(username, password);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestParam String token) {
        log.info("用户登出请求");
        return userService.logout(token);
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/current")
    public ApiResponse<User> getCurrentUser(@RequestParam String token) {
        log.info("获取当前用户信息请求");
        return userService.getCurrentUser(token);
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        log.info("获取用户统计信息请求");
        return userService.getUserStatistics();
    }

    /**
     * 获取用户类型统计
     */
    @GetMapping("/statistics/types")
    public ApiResponse<Map<String, Long>> getUserTypeStatistics() {
        log.info("获取用户类型统计请求");
        return userService.getUserTypeStatistics();
    }

    /**
     * 批量导入用户
     */
    @PostMapping("/import")
    public ApiResponse<Void> importUsers(@RequestBody List<User> users) {
        log.info("批量导入用户请求，用户数量：{}", users.size());
        return userService.importUsers(users);
    }

    /**
     * 导用户数据
     */
    @GetMapping("/export")
    public ApiResponse<List<User>> exportUsers(
            @RequestParam(required = false) String userType,
            @RequestParam(required = false) String status) {
        log.info("导出用户数据请求，用户类型：{}，状态：{}", userType, status);
        return userService.exportUsers(userType, status);
    }
} 