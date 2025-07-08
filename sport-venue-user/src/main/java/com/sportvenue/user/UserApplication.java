package com.sportvenue.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 用户服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.sportvenue.user", "com.sportvenue.common"})
public class UserApplication {
    public static void main(String[] args) {
        // 设置默认profile为local，用于IDE直接运行
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(UserApplication.class, args);
    }
} 