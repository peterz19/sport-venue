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
public class DailyProductReportDTO {
    private String date;
    private List<DailyProductStatDTO> items;
    private BigDecimal totalAmount;
    private Integer totalQty;
}
