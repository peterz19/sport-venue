package com.sportvenue.user.service;

/**
 * 用户服务接口
 */
public interface UserService {
    
    /**
     * 获取用户信息
     * @param userId 用户ID
     * @return 用户信息
     */
    String getUserInfo(Long userId);
} 