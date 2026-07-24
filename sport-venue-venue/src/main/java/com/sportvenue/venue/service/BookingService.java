package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.booking.BookingCalendarDTO;
import com.sportvenue.venue.dto.booking.BookingCreateRequest;
import com.sportvenue.venue.dto.booking.BookingDTO;
import org.springframework.data.domain.Page;

import java.util.Map;

public interface BookingService {
    ApiResponse<BookingCalendarDTO> calendar(Long courtId, String date);
    ApiResponse<Page<BookingDTO>> list(String date, Long venueId, Long courtId, String status,
                                       String bookType, int page, int size);
    ApiResponse<BookingDTO> detail(Long id);
    ApiResponse<BookingDTO> create(BookingCreateRequest request);
    ApiResponse<BookingDTO> cancel(Long id, Map<String, Object> body);
    ApiResponse<BookingDTO> complete(Long id);
}
