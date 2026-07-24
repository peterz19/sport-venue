package com.sportvenue.venue.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyProductStatDTO {
    private Long productId;
    private String productName;
    private String unit;
    private BigDecimal unitPrice;
    private Integer totalQty;
    private BigDecimal totalAmount;
    private Integer orderCount;
}
