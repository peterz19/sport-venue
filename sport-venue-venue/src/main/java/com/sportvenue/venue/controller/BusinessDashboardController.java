package com.sportvenue.venue.controller;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.dashboard.MerchantDashboardDTO;
import com.sportvenue.venue.entity.Booking;
import com.sportvenue.venue.entity.SalesOrder;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.repository.BookingRepository;
import com.sportvenue.venue.repository.SalesOrderRepository;
import com.sportvenue.venue.repository.VenueRepository;
import com.sportvenue.venue.util.SecurityUtils;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "B端经营看板")
@Slf4j
@RestController
@RequestMapping("/business/dashboard")
@CrossOrigin(origins = "*")
public class BusinessDashboardController {

    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private SalesOrderRepository salesOrderRepository;
    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/overview")
    public ApiResponse<MerchantDashboardDTO> overview(@RequestParam(value = "date", required = false) String date) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            LocalDate day = StringUtils.hasText(date) ? LocalDate.parse(date) : LocalDate.now();
            var start = day.atStartOfDay();
            var end = day.plusDays(1).atStartOfDay();

            List<Venue> venues = venueRepository.findByMerchantId(merchantId);
            List<SalesOrder> paid = salesOrderRepository.findPaidOrdersInRange(
                    merchantId, null, null, start, end, SalesOrder.OrderStatus.PAID);

            BigDecimal salesAmount = paid.stream()
                    .map(SalesOrder::getTotalAmount)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            var bookings = bookingRepository.search(
                    merchantId, null, null, null, null, null, start, end, PageRequest.of(0, 500)).getContent();

            int booked = 0, completed = 0, cancelled = 0;
            BigDecimal bookingAmount = BigDecimal.ZERO;
            for (Booking b : bookings) {
                if (b.getStatus() == Booking.BookingStatus.BOOKED) booked++;
                else if (b.getStatus() == Booking.BookingStatus.COMPLETED) completed++;
                else if (b.getStatus() == Booking.BookingStatus.CANCELLED) cancelled++;
                if (b.getStatus() == Booking.BookingStatus.BOOKED || b.getStatus() == Booking.BookingStatus.COMPLETED) {
                    bookingAmount = bookingAmount.add(b.getAmount() == null ? BigDecimal.ZERO : b.getAmount());
                }
            }

            List<Map<String, Object>> venueRows = venues.stream().map(v -> {
                Map<String, Object> m = new LinkedHashMap<>();
                m.put("id", v.getId());
                m.put("name", v.getName());
                m.put("status", v.getStatus() == null ? null : v.getStatus().name());
                m.put("type", v.getType() == null ? null : v.getType().name());
                m.put("currentOccupancy", v.getCurrentOccupancy());
                m.put("capacity", v.getCapacity());
                m.put("rating", v.getRating());
                m.put("address", v.getAddress());
                return m;
            }).collect(Collectors.toList());

            return ApiResponse.success(MerchantDashboardDTO.builder()
                    .date(day.toString())
                    .venueCount(venues.size())
                    .salesOrderCount(paid.size())
                    .salesAmount(salesAmount.setScale(2, RoundingMode.HALF_UP))
                    .bookingCount(booked + completed)
                    .bookingAmount(bookingAmount.setScale(2, RoundingMode.HALF_UP))
                    .bookingBooked(booked)
                    .bookingCompleted(completed)
                    .bookingCancelled(cancelled)
                    .venues(venueRows)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("经营看板查询失败", e);
            return ApiResponse.error("经营看板查询失败");
        }
    }
}
