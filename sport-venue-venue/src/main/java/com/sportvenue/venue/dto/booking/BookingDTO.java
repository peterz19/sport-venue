package com.sportvenue.venue.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class BookingDTO {
    private Long id;
    private String orderNo;
    private Long merchantId;
    private Long venueId;
    private String venueName;
    private Long courtId;
    private String courtName;
    private String startTime;
    private String endTime;
    private String bookType;
    private Long teamId;
    private String teamName;
    private String personName;
    private String personPhone;
    private Long operatorId;
    private String operatorName;
    private Long liaisonStaffId;
    private String liaisonStaffName;
    private String status;
    private BigDecimal amount;
    private String source;
    private String remark;
    private Long matchResultId;
}
