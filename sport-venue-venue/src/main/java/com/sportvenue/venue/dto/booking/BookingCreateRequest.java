package com.sportvenue.venue.dto.booking;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class BookingCreateRequest {
    private Long courtId;
    private String startTime;
    private String endTime;
    private String bookType;
    private Long teamId;
    private String personName;
    private String personPhone;
    private BigDecimal amount;
    private String remark;
}
