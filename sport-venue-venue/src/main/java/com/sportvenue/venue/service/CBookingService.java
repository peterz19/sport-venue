package com.sportvenue.venue.service;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CBookingService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<Booking.BookingStatus> OCCUPYING =
            Arrays.asList(Booking.BookingStatus.BOOKED, Booking.BookingStatus.COMPLETED);

    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private MerchantFeatureService featureService;
    @Autowired
    private WalletService walletService;

    public List<Map<String, Object>> listVenues() {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.C_END);
        return venueRepository.findByMerchantId(merchantId).stream()
                .filter(v -> v.getStatus() == Venue.VenueStatus.ACTIVE)
                .map(v -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", v.getId());
                    m.put("name", v.getName());
                    m.put("address", v.getAddress());
                    m.put("type", v.getType().name());
                    return m;
                }).collect(Collectors.toList());
    }

    public List<Map<String, Object>> listCourts(Long venueId) {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.C_END);
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.BOOKING);
        return courtRepository.findByMerchantIdOrderBySortOrderAscIdAsc(merchantId).stream()
                .filter(c -> venueId == null || Objects.equals(c.getVenueId(), venueId))
                .filter(c -> c.getStatus() == Court.CourtStatus.ACTIVE)
                .map(c -> {
                    Map<String, Object> m = new LinkedHashMap<>();
                    m.put("id", c.getId());
                    m.put("name", c.getName());
                    m.put("venueId", c.getVenueId());
                    return m;
                }).collect(Collectors.toList());
    }

    public Map<String, Object> slots(Long courtId, String date) {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.BOOKING);
        Court court = courtRepository.findByIdAndMerchantId(courtId, merchantId)
                .orElseThrow(() -> new BusinessException(404, "片场不存在"));
        LocalDate day = LocalDate.parse(date);
        LocalDateTime dayStart = day.atStartOfDay();
        LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
        List<Booking> bookings = bookingRepository.findDayOccupancy(courtId, dayStart, dayEnd, OCCUPYING);
        List<Map<String, Object>> slots = new ArrayList<>();
        for (int hour = 0; hour < 24; hour++) {
            for (int minute : new int[]{0, 30}) {
                LocalDateTime start = LocalDateTime.of(day, LocalTime.of(hour, minute));
                LocalDateTime end = start.plusMinutes(30);
                boolean occupied = bookings.stream()
                        .anyMatch(b -> b.getStartTime().isBefore(end) && b.getEndTime().isAfter(start));
                Map<String, Object> s = new LinkedHashMap<>();
                s.put("startTime", start.format(DT));
                s.put("endTime", end.format(DT));
                s.put("occupied", occupied);
                slots.add(s);
            }
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("courtId", court.getId());
        result.put("courtName", court.getName());
        result.put("date", date);
        result.put("slots", slots);
        return result;
    }

    @Transactional
    public Map<String, Object> create(Map<String, Object> body) {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.C_END);
        featureService.requireEnabled(merchantId, MerchantFeatureService.Feature.BOOKING);

        Long courtId = body.get("courtId") == null ? null : Long.valueOf(body.get("courtId").toString());
        String startTime = body.get("startTime") == null ? null : body.get("startTime").toString();
        String endTime = body.get("endTime") == null ? null : body.get("endTime").toString();
        BigDecimal amount = body.get("amount") == null ? BigDecimal.ZERO : new BigDecimal(body.get("amount").toString());
        if (courtId == null || !StringUtils.hasText(startTime) || !StringUtils.hasText(endTime)) {
            throw new BusinessException(400, "片场与时间必填");
        }
        Court court = courtRepository.findByIdAndMerchantId(courtId, merchantId)
                .orElseThrow(() -> new BusinessException(404, "片场不存在"));
        LocalDateTime start = LocalDateTime.parse(startTime, DT);
        LocalDateTime end = LocalDateTime.parse(endTime, DT);
        if (!end.isAfter(start) || start.getMinute() % 30 != 0 || end.getMinute() % 30 != 0) {
            throw new BusinessException(400, "时间须为半小时对齐且结束晚于开始");
        }
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            // 先占场再建扣款：用短事务内冲突检测 + 扣款
        }
        List<Booking> conflicts = bookingRepository.findConflicts(courtId, start, end, OCCUPYING, null);
        if (!conflicts.isEmpty()) {
            throw new BusinessException(409, "时段已被占用");
        }

        Booking booking = new Booking();
        booking.setOrderNo("CB" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + String.format("%04d", new Random().nextInt(10000)));
        booking.setMerchantId(merchantId);
        booking.setVenueId(court.getVenueId());
        booking.setCourtId(courtId);
        booking.setStartTime(start);
        booking.setEndTime(end);
        booking.setBookType(Booking.BookType.PERSON);
        booking.setPersonName(SecurityUtils.requireCustomer().getNickname());
        booking.setPersonPhone("");
        booking.setOperatorId(0L);
        booking.setOperatorName("C_USER");
        booking.setAmount(amount);
        booking.setSource("C");
        booking.setCustomerUserId(customerId);
        booking.setStatus(Booking.BookingStatus.BOOKED);
        Booking saved = bookingRepository.save(booking);
        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            walletService.debit(merchantId, customerId, amount, "BOOKING_PAY", saved.getId(), "订场扣款");
        }
        return toView(saved);
    }

    public List<Map<String, Object>> mine() {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        return bookingRepository
                .findTop50ByMerchantIdAndCustomerUserIdAndSourceOrderByStartTimeDesc(merchantId, customerId, "C")
                .stream()
                .map(this::toView)
                .collect(Collectors.toList());
    }

    @Transactional
    public Map<String, Object> cancel(Long id) {
        Long merchantId = SecurityUtils.requireCustomerMerchantId();
        Long customerId = SecurityUtils.requireCustomerId();
        Booking booking = bookingRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new BusinessException(404, "订场不存在"));
        if (!Objects.equals(booking.getCustomerUserId(), customerId) || !"C".equals(booking.getSource())) {
            throw new BusinessException(404, "订场不存在");
        }
        if (booking.getStatus() != Booking.BookingStatus.BOOKED) {
            throw new BusinessException(400, "仅已预订可取消");
        }
        booking.setStatus(Booking.BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        booking.setCancelReason("用户取消");
        bookingRepository.save(booking);
        if (booking.getAmount() != null && booking.getAmount().compareTo(BigDecimal.ZERO) > 0) {
            walletService.credit(merchantId, customerId, booking.getAmount(),
                    "BOOKING_REFUND", booking.getId(), "订场取消退款");
        }
        return toView(booking);
    }

    private Map<String, Object> toView(Booking b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", b.getId());
        m.put("orderNo", b.getOrderNo());
        m.put("courtId", b.getCourtId());
        m.put("venueId", b.getVenueId());
        m.put("startTime", b.getStartTime().format(DT));
        m.put("endTime", b.getEndTime().format(DT));
        m.put("amount", b.getAmount());
        m.put("status", b.getStatus().name());
        return m;
    }
}
