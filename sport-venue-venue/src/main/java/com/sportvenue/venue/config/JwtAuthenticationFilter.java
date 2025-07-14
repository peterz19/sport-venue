package com.sportvenue.venue.config;

import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.UserRepository;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * JWT认证过滤器
 * 用于验证JWT token并设置用户认证信息
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;

    @Autowired
    private UserRepository userRepository;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        log.debug("JWT过滤器检查路径: {}", path);
        
        // 跳过不需要认证的路径
        boolean shouldSkip = pathMatcher.match("/auth/login", path) ||
               pathMatcher.match("/auth/register", path) ||
               pathMatcher.match("/auth/dev/**", path) ||
               pathMatcher.match("/health/**", path) ||
               pathMatcher.match("/actuator/**", path) ||
               pathMatcher.match("/swagger-ui/**", path) ||
               pathMatcher.match("/v3/api-docs/**", path) ||
               pathMatcher.match("/swagger-ui.html", path) ||
               pathMatcher.match("/favicon.ico", path) ||
               pathMatcher.match("/error", path);
        
        log.debug("路径 {} 是否跳过JWT过滤: {}", path, shouldSkip);
        return shouldSkip;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        try {
            // 获取Authorization头
            String authHeader = request.getHeader("Authorization");
            
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                
                // 验证token
                if (jwtConfig.validateToken(token, jwtConfig.getUsernameFromToken(token))) {
                    // 从token中获取用户信息
                    String username = jwtConfig.getUsernameFromToken(token);
                    Long userId = jwtConfig.getUserIdFromToken(token);
                    String userType = jwtConfig.getUserTypeFromToken(token);
                    
                    // 获取用户详细信息
                    User user = userRepository.findByUsername(username).orElse(null);
                    
                    if (user != null && user.getStatus() == User.UserStatus.ACTIVE) {
                        // 构建权限列表
                        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                        
                        // 添加用户类型权限
                        authorities.add(new SimpleGrantedAuthority("ROLE_" + userType));
                        
                        // 添加ADMIN特殊权限
                        if ("ADMIN".equals(userType)) {
                            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                            authorities.add(new SimpleGrantedAuthority("ROLE_MERCHANT"));
                        }
                        
                        // 添加用户角色权限
                        if (user.getRoles() != null) {
                            user.getRoles().forEach(role -> {
                                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode()));
                            });
                        }
                        
                        // 创建认证对象
                        UsernamePasswordAuthenticationToken authentication = 
                            new UsernamePasswordAuthenticationToken(user, null, authorities);
                        
                        // 设置认证信息到Security上下文
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                        
                        log.debug("用户认证成功：{}，权限：{}", username, authorities);
                    } else {
                        log.warn("用户不存在或已被停用：{}", username);
                    }
                } else {
                    log.warn("JWT token验证失败");
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过滤器异常：{}", e.getMessage(), e);
        }
        
        filterChain.doFilter(request, response);
    }
} 