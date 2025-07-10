package com.sportvenue.venue.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * JPA配置类
 * 启用JPA审计功能和仓库扫描
 */
@Configuration
@EnableJpaAuditing
@EnableJpaRepositories(basePackages = "com.sportvenue.venue.repository")
public class JpaConfig {
} 