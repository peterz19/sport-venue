package com.sportvenue.social;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@ComponentScan(basePackages = {"com.sportvenue.social", "com.sportvenue.common"})
public class SocialApplication {
    
    public static void main(String[] args) {
        // 设置默认profile为local，用于IDE直接运行
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(SocialApplication.class, args);
    }
} 