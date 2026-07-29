package com.sportvenue.venue.dto.dashboard;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class MerchantDashboardDTO {
    private String date;
    private Integer venueCount;
    private Integer salesOrderCount;
    private BigDecimal salesAmount;
    private Integer bookingCount;
    private BigDecimal bookingAmount;
    private Integer bookingBooked;
    private Integer bookingCompleted;
    private Integer bookingCancelled;
    private List<Map<String, Object>> venues;
}
