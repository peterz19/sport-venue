package com.sportvenue.venue.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security配置
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF
            .csrf(csrf -> csrf.disable())
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            // 设置会话管理
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 配置授权规则
            .authorizeHttpRequests(authz -> authz
                // 允许认证相关接口
                .requestMatchers("/auth/login", "/auth/register").permitAll()
                // 允许开发专用接口
                .requestMatchers("/auth/dev/**").permitAll()
                // 允许健康检查接口
                .requestMatchers("/health", "/actuator/health").permitAll()
                // 允许Swagger文档
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                // 允许静态资源
                .requestMatchers("/favicon.ico", "/error").permitAll()
                // 其他所有请求需要认证
                .anyRequest().authenticated()
            )
            // 禁用HTTP Basic认证
            .httpBasic(basic -> basic.disable())
            // 禁用表单登录
            .formLogin(form -> form.disable())
            // 禁用登出页面
            .logout(logout -> logout.disable());

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(Arrays.asList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
} 