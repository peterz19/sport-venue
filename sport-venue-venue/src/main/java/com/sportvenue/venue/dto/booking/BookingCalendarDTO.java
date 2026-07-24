package com.sportvenue.venue.dto.booking;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class BookingCalendarDTO {
    private Long courtId;
    private String courtName;
    private String date;
    private List<Slot> slots;

    @Data
    @Builder
    public static class Slot {
        private String startTime;
        private String endTime;
        private boolean occupied;
        private Long bookingId;
        private String orderNo;
        private String bookType;
        private String title;
        private String status;
    }
}
