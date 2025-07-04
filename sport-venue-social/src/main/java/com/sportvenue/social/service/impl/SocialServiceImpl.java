package com.sportvenue.social.service.impl;

import com.sportvenue.social.service.SocialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 社交服务实现类
 */
@Slf4j
@Service
public class SocialServiceImpl implements SocialService {

    @Override
    public String getSocialFeed() {
        log.info("获取社交动态");
        return "欢迎来到体育场馆社交平台！";
    }
} 