package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.sales.*;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.service.PlatformCommissionService;
import com.sportvenue.venue.service.SalesService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class SalesServiceImpl implements SalesService {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter DATETIME = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SalesOrderRepository salesOrderRepository;
    @Autowired
    private SalesOrderItemRepository salesOrderItemRepository;
    @Autowired
    private SalesPaymentRepository salesPaymentRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private PlatformCommissionService platformCommissionService;

    @Override
    public ApiResponse<SalesPreviewResponse> preview(SalesPreviewRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            BuiltCart cart = buildCart(merchantId, request);
            return ApiResponse.success(SalesPreviewResponse.builder()
                    .venueId(cart.venue.getId())
                    .venueName(cart.venue.getName())
                    .totalAmount(cart.totalAmount)
                    .totalQty(cart.totalQty)
                    .itemCount(cart.items.size())
                    .items(cart.items)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("预览销售单失败", e);
            return ApiResponse.error("预览销售单失败");
        }
    }

    @Override
    public ApiResponse<SalesOrderDTO> createOrder(SalesPreviewRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            BuiltCart cart = buildCart(merchantId, request);

            SalesOrder order = new SalesOrder();
            order.setOrderNo(nextOrderNo());
            order.setMerchantId(merchantId);
            order.setVenueId(cart.venue.getId());
            order.setTotalAmount(cart.totalAmount);
            order.setItemCount(cart.items.size());
            order.setTotalQty(cart.totalQty);
            order.setStatus(SalesOrder.OrderStatus.PENDING);
            order.setOperatorId(SecurityUtils.currentUserId());
            order.setOperatorName(SecurityUtils.currentOperatorName());
            SalesOrder saved = salesOrderRepository.save(order);

            List<SalesOrderItem> entities = new ArrayList<>();
            for (SalesItemDTO line : cart.items) {
                SalesOrderItem item = new SalesOrderItem();
                item.setOrderId(saved.getId());
                item.setProductId(line.getProductId());
                item.setProductName(line.getProductName());
                item.setUnit(line.getUnit());
                item.setUnitPrice(line.getUnitPrice());
                item.setQuantity(line.getQuantity());
                item.setSubtotal(line.getSubtotal());
                entities.add(item);
            }
            salesOrderItemRepository.saveAll(entities);

            return ApiResponse.success(toOrderDto(saved, cart.venue.getName(), cart.items, placeholderPayment(), null));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建销售单失败", e);
            return ApiResponse.error("创建销售单失败");
        }
    }

    @Override
    public ApiResponse<SalesOrderDTO> payByCash(Long orderId, Map<String, Object> body) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            SalesOrder order = salesOrderRepository.findByIdAndMerchantId(orderId, merchantId)
                    .orElseThrow(() -> new BusinessException("订单不存在"));

            if (order.getStatus() == SalesOrder.OrderStatus.PAID
                    && order.getPayMethod() == SalesOrder.PayMethod.CASH) {
                String venueName = venueName(order.getVenueId());
                List<SalesItemDTO> items = loadItemDtos(order.getId());
                String paymentNo = salesPaymentRepository.findFirstByOrderIdAndStatus(orderId, SalesPayment.PaymentStatus.SUCCESS)
                        .map(SalesPayment::getPaymentNo).orElse(null);
                return ApiResponse.success(toOrderDto(order, venueName, items, null, paymentNo));
            }
            if (order.getStatus() != SalesOrder.OrderStatus.PENDING) {
                throw new BusinessException("仅待支付订单可确认现金收款");
            }

            if (body != null && body.get("receivedAmount") != null) {
                BigDecimal received = new BigDecimal(body.get("receivedAmount").toString());
                if (received.compareTo(order.getTotalAmount()) < 0) {
                    throw new BusinessException("实收金额不能小于应收金额");
                }
            }

            LocalDateTime now = LocalDateTime.now();
            order.setStatus(SalesOrder.OrderStatus.PAID);
            order.setPayMethod(SalesOrder.PayMethod.CASH);
            order.setPaidAt(now);
            if (body != null && body.get("remark") != null) {
                order.setRemark(String.valueOf(body.get("remark")));
            }
            salesOrderRepository.save(order);

            SalesPayment payment = new SalesPayment();
            payment.setOrderId(order.getId());
            payment.setPaymentNo(nextPaymentNo());
            payment.setPayMethod(SalesOrder.PayMethod.CASH);
            payment.setAmount(order.getTotalAmount());
            payment.setStatus(SalesPayment.PaymentStatus.SUCCESS);
            payment.setPaidAt(now);
            payment.setRemark("现金支付确认");
            salesPaymentRepository.save(payment);

            platformCommissionService.accrueFromSalesOrder(order);

            return ApiResponse.success(toOrderDto(order, venueName(order.getVenueId()),
                    loadItemDtos(order.getId()), null, payment.getPaymentNo()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("现金支付确认失败", e);
            return ApiResponse.error("现金支付确认失败");
        }
    }

    @Override
    public ApiResponse<Void> cancelOrder(Long orderId, Map<String, Object> body) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            SalesOrder order = salesOrderRepository.findByIdAndMerchantId(orderId, merchantId)
                    .orElseThrow(() -> new BusinessException("订单不存在"));
            if (order.getStatus() != SalesOrder.OrderStatus.PENDING) {
                throw new BusinessException("仅待支付订单可取消");
            }
            order.setStatus(SalesOrder.OrderStatus.CANCELLED);
            order.setCancelledAt(LocalDateTime.now());
            if (body != null && body.get("reason") != null) {
                order.setCancelReason(String.valueOf(body.get("reason")));
            }
            salesOrderRepository.save(order);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("取消销售单失败", e);
            return ApiResponse.error("取消销售单失败");
        }
    }

    @Override
    public ApiResponse<SalesOrderDTO> getOrder(Long orderId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            SalesOrder order = salesOrderRepository.findByIdAndMerchantId(orderId, merchantId)
                    .orElseThrow(() -> new BusinessException("订单不存在"));
            PaymentInfoDTO payment = order.getStatus() == SalesOrder.OrderStatus.PENDING
                    ? placeholderPayment() : null;
            String paymentNo = salesPaymentRepository.findFirstByOrderIdAndStatus(orderId, SalesPayment.PaymentStatus.SUCCESS)
                    .map(SalesPayment::getPaymentNo).orElse(null);
            return ApiResponse.success(toOrderDto(order, venueName(order.getVenueId()),
                    loadItemDtos(order.getId()), payment, paymentNo));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询销售单失败", e);
            return ApiResponse.error("查询销售单失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getOrderStatus(Long orderId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            SalesOrder order = salesOrderRepository.findByIdAndMerchantId(orderId, merchantId)
                    .orElseThrow(() -> new BusinessException("订单不存在"));
            Map<String, Object> data = new HashMap<>();
            data.put("orderId", order.getId());
            data.put("status", order.getStatus().name());
            data.put("paidAt", order.getPaidAt() == null ? null : DATETIME.format(order.getPaidAt()));
            return ApiResponse.success(data);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询订单状态失败", e);
            return ApiResponse.error("查询订单状态失败");
        }
    }

    @Override
    public ApiResponse<Page<SalesOrderDTO>> listOrders(String date, Long venueId, String status,
                                                       String payMethod, int page, int size) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Long operatorFilter = SecurityUtils.isOwner() ? null : SecurityUtils.currentUserId();
            LocalDate day = parseDate(date);
            LocalDateTime start = day == null ? null : day.atStartOfDay();
            LocalDateTime end = day == null ? null : day.plusDays(1).atStartOfDay();
            SalesOrder.OrderStatus statusEnum = StringUtils.hasText(status)
                    ? SalesOrder.OrderStatus.valueOf(status.toUpperCase()) : SalesOrder.OrderStatus.PAID;
            SalesOrder.PayMethod payEnum = StringUtils.hasText(payMethod)
                    ? SalesOrder.PayMethod.valueOf(payMethod.toUpperCase()) : null;

            Page<SalesOrder> orders = salesOrderRepository.searchOrders(
                    merchantId, venueId, statusEnum, payEnum, operatorFilter, start, end, PageRequest.of(page, size));
            Map<Long, String> venueNames = venueRepository.findByMerchantId(merchantId).stream()
                    .collect(Collectors.toMap(Venue::getId, Venue::getName, (a, b) -> a));

            return ApiResponse.success(orders.map(o ->
                    toOrderDto(o, venueNames.get(o.getVenueId()), loadItemDtos(o.getId()), null, null)));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询订单列表失败", e);
            return ApiResponse.error("查询订单列表失败");
        }
    }

    @Override
    public ApiResponse<DailySummaryDTO> dailySummary(String date, Long venueId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Long operatorFilter = SecurityUtils.isOwner() ? null : SecurityUtils.currentUserId();
            LocalDate day = parseDateRequired(date);
            List<SalesOrder> orders = salesOrderRepository.findPaidOrdersInRange(
                    merchantId, venueId, operatorFilter, day.atStartOfDay(), day.plusDays(1).atStartOfDay(),
                    SalesOrder.OrderStatus.PAID);

            BigDecimal totalAmount = BigDecimal.ZERO;
            int totalQty = 0;
            Map<SalesOrder.PayMethod, Aggregate> byMethod = new EnumMap<>(SalesOrder.PayMethod.class);
            for (SalesOrder.PayMethod method : SalesOrder.PayMethod.values()) {
                byMethod.put(method, new Aggregate());
            }
            for (SalesOrder order : orders) {
                totalAmount = totalAmount.add(order.getTotalAmount());
                totalQty += order.getTotalQty();
                Aggregate agg = byMethod.get(order.getPayMethod() == null ? SalesOrder.PayMethod.CASH : order.getPayMethod());
                agg.count++;
                agg.amount = agg.amount.add(order.getTotalAmount());
            }

            List<Map<String, Object>> methodRows = new ArrayList<>();
            methodRows.add(payRow("CASH", "现金", byMethod.get(SalesOrder.PayMethod.CASH)));
            methodRows.add(payRow("WECHAT", "微信", byMethod.get(SalesOrder.PayMethod.WECHAT)));
            methodRows.add(payRow("ALIPAY", "支付宝", byMethod.get(SalesOrder.PayMethod.ALIPAY)));

            String venueName = venueId == null ? "全部场馆" : venueName(venueId);
            return ApiResponse.success(DailySummaryDTO.builder()
                    .date(day.toString())
                    .venueId(venueId)
                    .venueName(venueName)
                    .orderCount(orders.size())
                    .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                    .totalQty(totalQty)
                    .byPayMethod(methodRows)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询日汇总失败", e);
            return ApiResponse.error("查询日汇总失败");
        }
    }

    @Override
    public ApiResponse<DailyProductReportDTO> dailyProducts(String date, Long venueId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Long operatorFilter = SecurityUtils.isOwner() ? null : SecurityUtils.currentUserId();
            LocalDate day = parseDateRequired(date);
            List<SalesOrder> orders = salesOrderRepository.findPaidOrdersInRange(
                    merchantId, venueId, operatorFilter, day.atStartOfDay(), day.plusDays(1).atStartOfDay(),
                    SalesOrder.OrderStatus.PAID);
            if (orders.isEmpty()) {
                return ApiResponse.success(DailyProductReportDTO.builder()
                        .date(day.toString())
                        .items(Collections.emptyList())
                        .totalAmount(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                        .totalQty(0)
                        .build());
            }

            List<Long> orderIds = orders.stream().map(SalesOrder::getId).collect(Collectors.toList());
            List<SalesOrderItem> items = salesOrderItemRepository.findByOrderIdIn(orderIds);

            Map<String, DailyProductStatDTO> map = new LinkedHashMap<>();
            Map<String, Set<Long>> orderSets = new HashMap<>();
            for (SalesOrderItem item : items) {
                String key = item.getProductId() + "|" + item.getProductName() + "|" + item.getUnitPrice();
                DailyProductStatDTO stat = map.computeIfAbsent(key, k -> DailyProductStatDTO.builder()
                        .productId(item.getProductId())
                        .productName(item.getProductName())
                        .unit(item.getUnit())
                        .unitPrice(item.getUnitPrice())
                        .totalQty(0)
                        .totalAmount(BigDecimal.ZERO)
                        .orderCount(0)
                        .build());
                stat.setTotalQty(stat.getTotalQty() + item.getQuantity());
                stat.setTotalAmount(stat.getTotalAmount().add(item.getSubtotal()));
                orderSets.computeIfAbsent(key, k -> new HashSet<>()).add(item.getOrderId());
            }
            for (Map.Entry<String, DailyProductStatDTO> entry : map.entrySet()) {
                entry.getValue().setOrderCount(orderSets.get(entry.getKey()).size());
                entry.getValue().setTotalAmount(entry.getValue().getTotalAmount().setScale(2, RoundingMode.HALF_UP));
            }

            List<DailyProductStatDTO> stats = new ArrayList<>(map.values());
            BigDecimal totalAmount = stats.stream().map(DailyProductStatDTO::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            int totalQty = stats.stream().mapToInt(DailyProductStatDTO::getTotalQty).sum();

            return ApiResponse.success(DailyProductReportDTO.builder()
                    .date(day.toString())
                    .items(stats)
                    .totalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP))
                    .totalQty(totalQty)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询日商品汇总失败", e);
            return ApiResponse.error("查询日商品汇总失败");
        }
    }

    private BuiltCart buildCart(Long merchantId, SalesPreviewRequest request) {
        if (request.getVenueId() == null || request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("请选择场馆和商品");
        }
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new BusinessException("场馆不存在"));
        if (!merchantId.equals(venue.getMerchantId())) {
            throw new BusinessException(403, "无权操作该场馆");
        }
        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            throw new BusinessException("场馆未处于营业状态");
        }

        Map<Long, Integer> qtyMap = new LinkedHashMap<>();
        for (SalesPreviewRequest.Item item : request.getItems()) {
            if (item.getQuantity() == null || item.getQuantity() < 1) {
                throw new BusinessException("商品数量至少为1");
            }
            qtyMap.merge(item.getProductId(), item.getQuantity(), Integer::sum);
        }

        List<SalesItemDTO> lines = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQty = 0;
        for (Map.Entry<Long, Integer> entry : qtyMap.entrySet()) {
            Product product = productRepository.findByIdAndMerchantIdAndDeletedFalse(entry.getKey(), merchantId)
                    .orElseThrow(() -> new BusinessException("商品不存在或已删除"));
            if (product.getStatus() != Product.ProductStatus.ON_SALE) {
                throw new BusinessException("商品已下架：" + product.getName());
            }
            if (product.getVenueId() != null && !product.getVenueId().equals(venue.getId())) {
                throw new BusinessException("商品不属于当前场馆：" + product.getName());
            }
            BigDecimal subtotal = product.getPrice().multiply(BigDecimal.valueOf(entry.getValue()))
                    .setScale(2, RoundingMode.HALF_UP);
            lines.add(SalesItemDTO.builder()
                    .productId(product.getId())
                    .productName(product.getName())
                    .unit(product.getUnit())
                    .unitPrice(product.getPrice())
                    .quantity(entry.getValue())
                    .subtotal(subtotal)
                    .build());
            totalAmount = totalAmount.add(subtotal);
            totalQty += entry.getValue();
        }
        return new BuiltCart(venue, lines, totalAmount.setScale(2, RoundingMode.HALF_UP), totalQty);
    }

    private synchronized String nextOrderNo() {
        String prefix = "SO" + LocalDate.now().format(DAY);
        Long max = salesOrderRepository.findMaxSeqByPrefix(prefix);
        long next = (max == null ? 0 : max) + 1;
        return prefix + String.format("%06d", next);
    }

    private synchronized String nextPaymentNo() {
        String prefix = "SP" + LocalDate.now().format(DAY);
        Long max = salesPaymentRepository.findMaxSeqByPrefix(prefix);
        long next = (max == null ? 0 : max) + 1;
        return prefix + String.format("%06d", next);
    }

    private PaymentInfoDTO placeholderPayment() {
        return PaymentInfoDTO.builder()
                .mode("QR_PLACEHOLDER")
                .qrCodeUrl(null)
                .qrCodeBase64(null)
                .tip("扫码支付即将接入，当前请使用现金支付")
                .build();
    }

    private List<SalesItemDTO> loadItemDtos(Long orderId) {
        return salesOrderItemRepository.findByOrderId(orderId).stream()
                .map(i -> SalesItemDTO.builder()
                        .productId(i.getProductId())
                        .productName(i.getProductName())
                        .unit(i.getUnit())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .subtotal(i.getSubtotal())
                        .build())
                .collect(Collectors.toList());
    }

    private SalesOrderDTO toOrderDto(SalesOrder order, String venueName, List<SalesItemDTO> items,
                                     PaymentInfoDTO payment, String paymentNo) {
        return SalesOrderDTO.builder()
                .orderId(order.getId())
                .orderNo(order.getOrderNo())
                .status(order.getStatus().name())
                .payMethod(order.getPayMethod() == null ? null : order.getPayMethod().name())
                .merchantId(order.getMerchantId())
                .venueId(order.getVenueId())
                .venueName(venueName)
                .totalAmount(order.getTotalAmount())
                .totalQty(order.getTotalQty())
                .itemCount(order.getItemCount())
                .operatorName(order.getOperatorName())
                .items(items)
                .payment(payment)
                .paidAt(order.getPaidAt() == null ? null : DATETIME.format(order.getPaidAt()))
                .createTime(order.getCreateTime() == null ? null : DATETIME.format(order.getCreateTime()))
                .paymentNo(paymentNo)
                .build();
    }

    private String venueName(Long venueId) {
        return venueRepository.findById(venueId).map(Venue::getName).orElse("");
    }

    private LocalDate parseDate(String date) {
        if (!StringUtils.hasText(date)) {
            return null;
        }
        return LocalDate.parse(date);
    }

    private LocalDate parseDateRequired(String date) {
        if (!StringUtils.hasText(date)) {
            return LocalDate.now();
        }
        return LocalDate.parse(date);
    }

    private Map<String, Object> payRow(String method, String label, Aggregate agg) {
        Map<String, Object> row = new HashMap<>();
        row.put("payMethod", method);
        row.put("label", label);
        row.put("orderCount", agg.count);
        row.put("amount", agg.amount.setScale(2, RoundingMode.HALF_UP));
        return row;
    }

    private static class BuiltCart {
        private final Venue venue;
        private final List<SalesItemDTO> items;
        private final BigDecimal totalAmount;
        private final int totalQty;

        private BuiltCart(Venue venue, List<SalesItemDTO> items, BigDecimal totalAmount, int totalQty) {
            this.venue = venue;
            this.items = items;
            this.totalAmount = totalAmount;
            this.totalQty = totalQty;
        }
    }

    private static class Aggregate {
        private int count;
        private BigDecimal amount = BigDecimal.ZERO;
    }
}
