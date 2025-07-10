package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.User;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 用户服务接口
 */
public interface UserService {

    /**
     * 创建用户
     */
    ApiResponse<User> createUser(User user);

    /**
     * 更新用户
     */
    ApiResponse<User> updateUser(Long id, User user);

    /**
     * 删除用户
     */
    ApiResponse<Void> deleteUser(Long id);

    /**
     * 根据ID获取用户详情
     */
    ApiResponse<User> getUserById(Long id);

    /**
     * 分页查询用户列表
     */
    ApiResponse<Page<User>> getUserList(Integer page, Integer size, String username, String userType, String status);

    /**
     * 根据商户ID查询用户
     */
    ApiResponse<List<User>> getUsersByMerchant(Long merchantId);

    /**
     * 更新用户状态
     */
    ApiResponse<Void> updateUserStatus(Long id, User.UserStatus status);

    /**
     * 重置用户密码
     */
    ApiResponse<Void> resetUserPassword(Long id);

    /**
     * 修改用户密码
     */
    ApiResponse<Void> changeUserPassword(Long id, String oldPassword, String newPassword);

    /**
     * 分配用户角色
     */
    ApiResponse<Void> assignUserRoles(Long id, List<Long> roleIds);

    /**
     * 获取用户角色
     */
    ApiResponse<List<Map<String, Object>>> getUserRoles(Long id);

    /**
     * 获取用户权限
     */
    ApiResponse<List<Map<String, Object>>> getUserPermissions(Long id);

    /**
     * 用户登录
     */
    ApiResponse<Map<String, Object>> login(String username, String password);

    /**
     * 用户登出
     */
    ApiResponse<Void> logout(String token);

    /**
     * 获取当前用户信息
     */
    ApiResponse<User> getCurrentUser(String token);

    /**
     * 获取用户统计信息
     */
    ApiResponse<Map<String, Object>> getUserStatistics();

    /**
     * 获取用户类型统计
     */
    ApiResponse<Map<String, Long>> getUserTypeStatistics();

    /**
     * 批量导入用户
     */
    ApiResponse<Void> importUsers(List<User> users);

    /**
     * 导出用户数据
     */
    ApiResponse<List<User>> exportUsers(String userType, String status);
} 