package com.sportvenue.venue.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI配置类
 * 配置Swagger文档信息
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("场馆服务API文档")
                        .description("体育场馆管理系统场馆服务模块API接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("SportVenue Team")
                                .email("support@sportvenue.com")
                                .url("https://www.sportvenue.com"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8082").description("本地开发环境"),
                        new Server().url("https://api.sportvenue.com").description("生产环境")
                ));
    }
} 