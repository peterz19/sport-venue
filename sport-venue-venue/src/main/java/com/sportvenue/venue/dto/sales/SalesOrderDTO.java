package com.sportvenue.venue.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderDTO {
    private Long orderId;
    private String orderNo;
    private String status;
    private String payMethod;
    private Long merchantId;
    private Long venueId;
    private String venueName;
    private BigDecimal totalAmount;
    private Integer totalQty;
    private Integer itemCount;
    private String operatorName;
    private List<SalesItemDTO> items;
    private PaymentInfoDTO payment;
    private String paidAt;
    private String createTime;
    private String paymentNo;
}
