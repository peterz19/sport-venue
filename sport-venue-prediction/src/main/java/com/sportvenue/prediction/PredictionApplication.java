package com.sportvenue.prediction;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * 预测服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
public class PredictionApplication {
    public static void main(String[] args) {
        SpringApplication.run(PredictionApplication.class, args);
    }
} 