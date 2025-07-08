package com.sportvenue.venue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.server.standard.ServerEndpointExporter;

/**
 * 场馆服务启动类
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableWebSocket
@EnableScheduling
@ComponentScan(basePackages = {"com.sportvenue.venue", "com.sportvenue.common"})
public class VenueApplication {
    public static void main(String[] args) {
        // 设置默认profile为local，用于IDE直接运行
        System.setProperty("spring.profiles.active", "local");
        SpringApplication.run(VenueApplication.class, args);
    }

    @Bean
    public ServerEndpointExporter serverEndpointExporter() {
        return new ServerEndpointExporter();
    }
} 