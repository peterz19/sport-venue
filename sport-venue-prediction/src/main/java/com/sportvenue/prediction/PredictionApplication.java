package com.sportvenue.prediction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

/**
 * 预测服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {"com.sportvenue.prediction", "com.sportvenue.common"})
public class PredictionApplication {
    public static void main(String[] args) {
        // 设置默认profile为local，用于IDE直接运行
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(PredictionApplication.class, args);
    }
} 