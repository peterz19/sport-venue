package com.sportvenue.venue.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.service.MerchantFeatureService;
import com.sportvenue.venue.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MerchantFeatureInterceptor implements HandlerInterceptor {

    @Autowired
    private MerchantFeatureService featureService;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getServletPath();
        if (path == null) {
            path = request.getRequestURI();
        }
        MerchantFeatureService.Feature feature = resolve(path);
        if (feature == null) {
            return true;
        }
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            featureService.requireEnabled(merchantId, feature);
            return true;
        } catch (Exception e) {
            response.setStatus(200);
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            int code = 40303;
            String msg = e.getMessage() != null ? e.getMessage() : "功能未开通";
            if (e instanceof com.sportvenue.common.exception.BusinessException be) {
                code = be.getCode();
                msg = be.getMessage();
            }
            objectMapper.writeValue(response.getWriter(), ApiResponse.error(code, msg));
            return false;
        }
    }

    private MerchantFeatureService.Feature resolve(String path) {
        if (path.startsWith("/business/products") || path.startsWith("/business/sales")) {
            return MerchantFeatureService.Feature.CASHIER;
        }
        if (path.startsWith("/business/bookings") || path.startsWith("/business/courts")) {
            return MerchantFeatureService.Feature.BOOKING;
        }
        if (path.startsWith("/business/teams") || path.startsWith("/business/matches")
                || path.startsWith("/business/ranking")) {
            return MerchantFeatureService.Feature.TEAM_MATCH;
        }
        return null;
    }
}
