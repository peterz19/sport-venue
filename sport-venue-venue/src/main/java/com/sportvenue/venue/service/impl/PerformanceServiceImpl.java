package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.StaffPerformanceDTO;
import com.sportvenue.venue.dto.staff.StaffPerformanceRankDTO;
import com.sportvenue.venue.entity.Booking;
import com.sportvenue.venue.entity.SalesOrder;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.BookingRepository;
import com.sportvenue.venue.repository.SalesOrderRepository;
import com.sportvenue.venue.repository.UserRepository;
import com.sportvenue.venue.service.PerformanceService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional(readOnly = true)
public class PerformanceServiceImpl implements PerformanceService {

    private static final List<Booking.BookingStatus> BOOKING_PERF =
            Arrays.asList(Booking.BookingStatus.BOOKED, Booking.BookingStatus.COMPLETED);

    @Autowired
    private SalesOrderRepository salesOrderRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private UserRepository userRepository;

    @Override
    public ApiResponse<StaffPerformanceDTO> myPerformance(String date, String dateFrom, String dateTo) {
        try {
            return ApiResponse.success(buildOne(SecurityUtils.currentUserId(), date, dateFrom, dateTo));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询我的业绩失败", e);
            return ApiResponse.error("查询我的业绩失败");
        }
    }

    @Override
    public ApiResponse<StaffPerformanceRankDTO> staffRank(String date, String dateFrom, String dateTo) {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            LocalDate[] range = resolveRange(date, dateFrom, dateTo);
            LocalDateTime start = range[0].atStartOfDay();
            LocalDateTime end = range[1].plusDays(1).atStartOfDay();

            Map<Long, Agg> salesMap = new HashMap<>();
            for (Object[] row : salesOrderRepository.aggregatePaidByOperator(
                    merchantId, start, end, SalesOrder.OrderStatus.PAID)) {
                Agg agg = new Agg();
                agg.count = ((Number) row[1]).intValue();
                agg.amount = new BigDecimal(row[2].toString());
                agg.qty = ((Number) row[3]).intValue();
                salesMap.put((Long) row[0], agg);
            }

            Map<Long, BookAgg> operateMap = new HashMap<>();
            for (Object[] row : bookingRepository.aggregateByOperator(merchantId, start, end, BOOKING_PERF)) {
                BookAgg agg = new BookAgg();
                agg.count = ((Number) row[1]).intValue();
                agg.amount = new BigDecimal(row[2].toString());
                operateMap.put((Long) row[0], agg);
            }

            Map<Long, BookAgg> liaisonMap = new HashMap<>();
            for (Object[] row : bookingRepository.aggregateByLiaison(
                    merchantId, start, end, BOOKING_PERF, Booking.BookType.TEAM)) {
                BookAgg agg = new BookAgg();
                agg.count = ((Number) row[1]).intValue();
                agg.amount = new BigDecimal(row[2].toString());
                liaisonMap.put((Long) row[0], agg);
            }

            List<User> staffList = userRepository.findByMerchantIdAndUserTypeIn(
                    merchantId, Arrays.asList(User.UserType.B_MERCHANT, User.UserType.B_STAFF));

            List<StaffPerformanceDTO> items = staffList.stream()
                    .map(u -> toDto(u, range[0], range[1],
                            salesMap.getOrDefault(u.getId(), new Agg()),
                            operateMap.getOrDefault(u.getId(), new BookAgg()),
                            liaisonMap.getOrDefault(u.getId(), new BookAgg())))
                    .sorted((a, b) -> {
                        int c = b.getSalesAmount().compareTo(a.getSalesAmount());
                        if (c != 0) return c;
                        return Integer.compare(
                                b.getBookingOperateCount() + b.getBookingLiaisonCount(),
                                a.getBookingOperateCount() + a.getBookingLiaisonCount());
                    })
                    .collect(Collectors.toList());

            return ApiResponse.success(StaffPerformanceRankDTO.builder()
                    .dateFrom(range[0].toString())
                    .dateTo(range[1].toString())
                    .items(items)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询员工业绩排行失败", e);
            return ApiResponse.error("查询员工业绩排行失败");
        }
    }

    @Override
    public ApiResponse<StaffPerformanceDTO> staffPerformance(Long staffId, String date, String dateFrom, String dateTo) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            User current = SecurityUtils.requireCurrentUser();
            if (!SecurityUtils.isOwner() && !current.getId().equals(staffId)) {
                throw new BusinessException(403, "店员只能查看自己的业绩");
            }
            User staff = userRepository.findByIdAndMerchantId(staffId, merchantId)
                    .orElseThrow(() -> new BusinessException("员工不存在"));
            if (staff.getUserType() != User.UserType.B_MERCHANT && staff.getUserType() != User.UserType.B_STAFF) {
                throw new BusinessException("非商户员工");
            }
            return ApiResponse.success(buildOne(staffId, date, dateFrom, dateTo));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询员工业绩失败", e);
            return ApiResponse.error("查询员工业绩失败");
        }
    }

    private StaffPerformanceDTO buildOne(Long staffId, String date, String dateFrom, String dateTo) {
        Long merchantId = SecurityUtils.requireMerchantId();
        LocalDate[] range = resolveRange(date, dateFrom, dateTo);
        User staff = userRepository.findByIdAndMerchantId(staffId, merchantId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        LocalDateTime start = range[0].atStartOfDay();
        LocalDateTime end = range[1].plusDays(1).atStartOfDay();

        Agg sales = new Agg();
        for (SalesOrder order : salesOrderRepository.findPaidOrdersInRange(
                merchantId, null, staffId, start, end, SalesOrder.OrderStatus.PAID)) {
            sales.count++;
            sales.amount = sales.amount.add(order.getTotalAmount());
            sales.qty += order.getTotalQty() == null ? 0 : order.getTotalQty();
        }

        BookAgg operate = firstBookAgg(bookingRepository.aggregateOperateOne(
                merchantId, staffId, start, end, BOOKING_PERF));
        BookAgg liaison = firstBookAgg(bookingRepository.aggregateLiaisonOne(
                merchantId, staffId, start, end, BOOKING_PERF, Booking.BookType.TEAM));

        return toDto(staff, range[0], range[1], sales, operate, liaison);
    }

    private BookAgg firstBookAgg(List<Object[]> rows) {
        BookAgg agg = new BookAgg();
        if (rows == null || rows.isEmpty() || rows.get(0) == null) {
            return agg;
        }
        Object[] row = rows.get(0);
        if (row.length >= 2 && row[0] != null) {
            agg.count = ((Number) row[0]).intValue();
            agg.amount = new BigDecimal(row[1].toString());
        }
        return agg;
    }

    private StaffPerformanceDTO toDto(User staff, LocalDate from, LocalDate to,
                                      Agg sales, BookAgg operate, BookAgg liaison) {
        String role = staff.getUserType() == User.UserType.B_MERCHANT ? "OWNER" : "STAFF";
        String name = StringUtils.hasText(staff.getRealName()) ? staff.getRealName() : staff.getUsername();
        return StaffPerformanceDTO.builder()
                .staffId(staff.getId())
                .staffName(name)
                .role(role)
                .dateFrom(from.toString())
                .dateTo(to.toString())
                .salesOrderCount(sales.count)
                .salesAmount(sales.amount.setScale(2, RoundingMode.HALF_UP))
                .salesQty(sales.qty)
                .bookingOperateCount(operate.count)
                .bookingOperateAmount(operate.amount.setScale(2, RoundingMode.HALF_UP))
                .bookingLiaisonCount(liaison.count)
                .bookingLiaisonAmount(liaison.amount.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    private LocalDate[] resolveRange(String date, String dateFrom, String dateTo) {
        if (StringUtils.hasText(date)) {
            LocalDate d = LocalDate.parse(date);
            return new LocalDate[]{d, d};
        }
        LocalDate from = StringUtils.hasText(dateFrom) ? LocalDate.parse(dateFrom) : LocalDate.now();
        LocalDate to = StringUtils.hasText(dateTo) ? LocalDate.parse(dateTo) : from;
        if (to.isBefore(from)) {
            throw new BusinessException("结束日期不能早于开始日期");
        }
        return new LocalDate[]{from, to};
    }

    private static class Agg {
        private int count;
        private BigDecimal amount = BigDecimal.ZERO;
        private int qty;
    }

    private static class BookAgg {
        private int count;
        private BigDecimal amount = BigDecimal.ZERO;
    }
}
