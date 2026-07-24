package com.sportvenue.venue.dto.sales;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailySummaryDTO {
    private String date;
    private Long venueId;
    private String venueName;
    private Integer orderCount;
    private BigDecimal totalAmount;
    private Integer totalQty;
    private List<Map<String, Object>> byPayMethod;
}
