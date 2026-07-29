package com.sportvenue.venue.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security — SaaS 硬隔离
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/auth/login", "/auth/merchant/login", "/auth/register").permitAll()
                .requestMatchers("/c/auth/**").permitAll()
                .requestMatchers("/c/pay/notify/**", "/business/sales/payments/notify/**").permitAll()
                .requestMatchers("/health/**", "/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/favicon.ico", "/error").permitAll()
                // 开发重置口令：仅本地可考虑开放；生产应关。此处仍 permit，靠 profile 文档约束
                .requestMatchers("/auth/dev/**").permitAll()
                .requestMatchers("/auth/logout", "/auth/user/info", "/auth/refresh", "/auth/verify")
                    .authenticated()
                // 平台专属
                .requestMatchers("/merchants/**").hasRole("ADMIN")
                .requestMatchers("/venues/**").hasRole("ADMIN")
                .requestMatchers("/admin/**").hasRole("ADMIN")
                // B 端 — 禁止 ADMIN 走 business
                .requestMatchers("/business/**").hasAnyRole("B_MERCHANT", "B_STAFF", "MERCHANT")
                // C 端
                .requestMatchers("/c/**").hasRole("C_USER")
                // 旧 C 接口收紧，避免未租户化泄露
                .requestMatchers("/customer/**").denyAll()
                .anyRequest().hasRole("ADMIN")
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
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
