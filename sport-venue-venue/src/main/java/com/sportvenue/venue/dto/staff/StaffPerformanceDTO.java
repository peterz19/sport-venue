package com.sportvenue.venue.dto.staff;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class StaffPerformanceDTO {
    private Long staffId;
    private String staffName;
    private String role;
    private String dateFrom;
    private String dateTo;
    private Integer salesOrderCount;
    private BigDecimal salesAmount;
    private Integer salesQty;
    private Integer bookingOperateCount;
    private BigDecimal bookingOperateAmount;
    private Integer bookingLiaisonCount;
    private BigDecimal bookingLiaisonAmount;
}
