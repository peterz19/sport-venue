package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.booking.BookingCalendarDTO;
import com.sportvenue.venue.dto.booking.BookingCreateRequest;
import com.sportvenue.venue.dto.booking.BookingDTO;
import com.sportvenue.venue.service.BookingService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "B端订场")
@RestController
@RequestMapping("/business/bookings")
@CrossOrigin(origins = "*")
public class BusinessBookingController {

    @Autowired
    private BookingService bookingService;

    @GetMapping("/calendar")
    public ApiResponse<BookingCalendarDTO> calendar(
            @RequestParam("courtId") Long courtId,
            @RequestParam("date") String date) {
        return bookingService.calendar(courtId, date);
    }

    @GetMapping
    public ApiResponse<Page<BookingDTO>> list(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "venueId", required = false) Long venueId,
            @RequestParam(value = "courtId", required = false) Long courtId,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "bookType", required = false) String bookType,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "20") int size) {
        return bookingService.list(date, venueId, courtId, status, bookType, page, size);
    }

    @GetMapping("/{id}")
    public ApiResponse<BookingDTO> detail(@PathVariable("id") Long id) {
        return bookingService.detail(id);
    }

    @PostMapping
    public ApiResponse<BookingDTO> create(@RequestBody BookingCreateRequest request) {
        return bookingService.create(request);
    }

    @PostMapping("/{id}/cancel")
    public ApiResponse<BookingDTO> cancel(@PathVariable("id") Long id,
                                          @RequestBody(required = false) Map<String, Object> body) {
        return bookingService.cancel(id, body == null ? Map.of() : body);
    }

    @PostMapping("/{id}/complete")
    public ApiResponse<BookingDTO> complete(@PathVariable("id") Long id) {
        return bookingService.complete(id);
    }
}
