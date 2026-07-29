package com.sportvenue.venue.config;

import com.sportvenue.venue.entity.CustomerUser;
import com.sportvenue.venue.entity.Merchant;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.CustomerUserRepository;
import com.sportvenue.venue.repository.MerchantRepository;
import com.sportvenue.venue.repository.UserRepository;
import com.sportvenue.venue.security.CustomerPrincipal;
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

@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtConfig jwtConfig;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerUserRepository customerUserRepository;
    @Autowired
    private MerchantRepository merchantRepository;

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = resolvePath(request);
        return pathMatcher.match("/auth/login", path)
                || pathMatcher.match("/auth/merchant/login", path)
                || pathMatcher.match("/auth/register", path)
                || pathMatcher.match("/auth/dev/**", path)
                || pathMatcher.match("/c/auth/**", path)
                || pathMatcher.match("/c/pay/notify/**", path)
                || pathMatcher.match("/business/sales/payments/notify/**", path)
                || pathMatcher.match("/health/**", path)
                || pathMatcher.match("/actuator/health", path)
                || pathMatcher.match("/swagger-ui/**", path)
                || pathMatcher.match("/v3/api-docs/**", path)
                || pathMatcher.match("/swagger-ui.html", path)
                || pathMatcher.match("/favicon.ico", path)
                || pathMatcher.match("/error", path);
    }

    private String resolvePath(HttpServletRequest request) {
        String path = request.getRequestURI();
        String servletPath = request.getServletPath();
        if (StringUtils.hasText(servletPath)) {
            return servletPath;
        }
        String contextPath = request.getContextPath();
        if (StringUtils.hasText(contextPath) && path.startsWith(contextPath)) {
            return path.substring(contextPath.length());
        }
        return path;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            String authHeader = request.getHeader("Authorization");
            if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);
                String username = jwtConfig.getUsernameFromToken(token);
                if (jwtConfig.validateToken(token, username)) {
                    String userType = jwtConfig.getUserTypeFromToken(token);
                    Long userId = jwtConfig.getUserIdFromToken(token);

                    if ("C_USER".equals(userType)) {
                        authenticateCustomer(userId);
                    } else {
                        authenticateStaff(username, userType, resolvePath(request));
                    }
                }
            }
        } catch (Exception e) {
            log.error("JWT认证过滤器异常：{}", e.getMessage(), e);
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);
    }

    private void authenticateCustomer(Long customerId) {
        if (customerId == null) {
            return;
        }
        CustomerUser customer = customerUserRepository.findById(customerId).orElse(null);
        if (customer == null || customer.getStatus() != CustomerUser.Status.ACTIVE) {
            log.warn("C用户不存在或已停用：{}", customerId);
            return;
        }
        if (!isMerchantActive(customer.getMerchantId())) {
            log.warn("商户已停用，拒绝 C 端 token：merchantId={}", customer.getMerchantId());
            return;
        }
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_C_USER"));
        CustomerPrincipal principal = new CustomerPrincipal(customer.getId(), customer.getMerchantId(), customer.getNickname());
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, authorities));
    }

    private void authenticateStaff(String username, String userType, String path) {
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getStatus() != User.UserStatus.ACTIVE) {
            log.warn("用户不存在或已被停用：{}", username);
            return;
        }
        // B 端请求二次校验商户状态
        if (path != null && path.startsWith("/business") && user.getMerchantId() != null) {
            if (!isMerchantActive(user.getMerchantId())) {
                log.warn("商户已停用，拒绝 business 请求：{}", user.getMerchantId());
                return;
            }
        }

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + userType));
        if ("ADMIN".equals(userType)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if ("B_MERCHANT".equals(userType) || "B_STAFF".equals(userType)) {
            authorities.add(new SimpleGrantedAuthority("ROLE_MERCHANT"));
        }
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode())));
        }
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, authorities));
    }

    private boolean isMerchantActive(Long merchantId) {
        return merchantRepository.findById(merchantId)
                .map(m -> m.getStatus() == Merchant.MerchantStatus.ACTIVE)
                .orElse(false);
    }
}
