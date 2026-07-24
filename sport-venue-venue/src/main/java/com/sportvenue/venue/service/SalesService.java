package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.*;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface SalesService {
    ApiResponse<SalesPreviewResponse> preview(SalesPreviewRequest request);

    ApiResponse<SalesOrderDTO> createOrder(SalesPreviewRequest request);

    ApiResponse<SalesOrderDTO> payByCash(Long orderId, Map<String, Object> body);

    ApiResponse<Void> cancelOrder(Long orderId, Map<String, Object> body);

    ApiResponse<SalesOrderDTO> getOrder(Long orderId);

    ApiResponse<Map<String, Object>> getOrderStatus(Long orderId);

    ApiResponse<Page<SalesOrderDTO>> listOrders(String date, Long venueId, String status,
                                                String payMethod, int page, int size);

    ApiResponse<DailySummaryDTO> dailySummary(String date, Long venueId);

    ApiResponse<DailyProductReportDTO> dailyProducts(String date, Long venueId);
}
