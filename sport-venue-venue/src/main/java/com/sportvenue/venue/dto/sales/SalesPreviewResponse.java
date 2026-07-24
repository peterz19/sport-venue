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
public class SalesPreviewResponse {
    private Long venueId;
    private String venueName;
    private BigDecimal totalAmount;
    private Integer totalQty;
    private Integer itemCount;
    private List<SalesItemDTO> items;
}
