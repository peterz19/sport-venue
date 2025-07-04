package com.sportvenue.user.service.impl;

import com.sportvenue.user.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Override
    public String getUserInfo(Long userId) {
        log.info("获取用户 {} 的信息", userId);
        return "用户 " + userId + " 的信息";
    }
} 