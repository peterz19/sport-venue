package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.*;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.repository.VenueRepository;
import com.sportvenue.venue.service.SalesService;
import com.sportvenue.venue.util.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "B端收银销售", description = "收银台、现金收款、销售报表")
@Slf4j
@RestController
@RequestMapping("/business")
@CrossOrigin(origins = "*")
public class BusinessSalesController {

    @Autowired
    private SalesService salesService;

    @Autowired
    private VenueRepository venueRepository;

    @Operation(summary = "我的场馆")
    @GetMapping("/venues/mine")
    public ApiResponse<List<Map<String, Object>>> myVenues() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            List<Map<String, Object>> list = venueRepository.findByMerchantId(merchantId).stream()
                    .filter(v -> v.getStatus() == Venue.VenueStatus.ACTIVE
                            || v.getStatus() == Venue.VenueStatus.MAINTENANCE)
                    .map(v -> {
                        Map<String, Object> m = new HashMap<>();
                        m.put("id", v.getId());
                        m.put("name", v.getName());
                        m.put("status", v.getStatus().name());
                        return m;
                    })
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (Exception e) {
            log.error("查询我的场馆失败", e);
            return ApiResponse.error(e.getMessage());
        }
    }

    @Operation(summary = "预览销售金额")
    @PostMapping("/sales/preview")
    public ApiResponse<SalesPreviewResponse> preview(@RequestBody SalesPreviewRequest request) {
        return salesService.preview(request);
    }

    @Operation(summary = "创建销售单")
    @PostMapping("/sales/orders")
    public ApiResponse<SalesOrderDTO> createOrder(@RequestBody SalesPreviewRequest request) {
        return salesService.createOrder(request);
    }

    @Operation(summary = "现金支付确认")
    @PostMapping("/sales/orders/{id}/pay/cash")
    public ApiResponse<SalesOrderDTO> payCash(@PathVariable("id") Long id,
                                              @RequestBody(required = false) Map<String, Object> body) {
        return salesService.payByCash(id, body == null ? Map.of() : body);
    }

    @Operation(summary = "取消销售单")
    @PostMapping("/sales/orders/{id}/cancel")
    public ApiResponse<Void> cancel(@PathVariable("id") Long id,
                                    @RequestBody(required = false) Map<String, Object> body) {
        return salesService.cancelOrder(id, body == null ? Map.of() : body);
    }

    @Operation(summary = "销售单详情")
    @GetMapping("/sales/orders/{id}")
    public ApiResponse<SalesOrderDTO> detail(@PathVariable("id") Long id) {
        return salesService.getOrder(id);
    }

    @Operation(summary = "销售单状态")
    @GetMapping("/sales/orders/{id}/status")
    public ApiResponse<Map<String, Object>> status(@PathVariable("id") Long id) {
        return salesService.getOrderStatus(id);
    }

    @Operation(summary = "销售单列表")
    @GetMapping("/sales/orders")
    public ApiResponse<Page<SalesOrderDTO>> list(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "payMethod", required = false) String payMethod,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return salesService.listOrders(date, venueId, status, payMethod, page, size);
    }

    @Operation(summary = "日销售汇总")
    @GetMapping("/sales/daily/summary")
    public ApiResponse<DailySummaryDTO> dailySummary(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return salesService.dailySummary(date, venueId);
    }

    @Operation(summary = "日商品汇总")
    @GetMapping("/sales/daily/products")
    public ApiResponse<DailyProductReportDTO> dailyProducts(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "venueId", required = false) Long venueId) {
        return salesService.dailyProducts(date, venueId);
    }
}
