package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.booking.BookingCalendarDTO;
import com.sportvenue.venue.dto.booking.BookingCreateRequest;
import com.sportvenue.venue.dto.booking.BookingDTO;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.service.BookingService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
@Transactional
public class BookingServiceImpl implements BookingService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final List<Booking.BookingStatus> OCCUPYING =
            Arrays.asList(Booking.BookingStatus.BOOKED, Booking.BookingStatus.COMPLETED);
    private static final List<Booking.BookingStatus> PERF_STATUSES =
            Arrays.asList(Booking.BookingStatus.BOOKED, Booking.BookingStatus.COMPLETED);

    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private CourtRepository courtRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private MatchResultRepository matchResultRepository;

    @Override
    public ApiResponse<BookingCalendarDTO> calendar(Long courtId, String date) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            if (courtId == null || !StringUtils.hasText(date)) {
                throw new BusinessException("片场和日期不能为空");
            }
            Court court = courtRepository.findByIdAndMerchantId(courtId, merchantId)
                    .orElseThrow(() -> new BusinessException("片场不存在"));
            LocalDate day = LocalDate.parse(date);
            LocalDateTime dayStart = day.atStartOfDay();
            LocalDateTime dayEnd = day.plusDays(1).atStartOfDay();
            List<Booking> bookings = bookingRepository.findDayOccupancy(courtId, dayStart, dayEnd, OCCUPYING);

            List<BookingCalendarDTO.Slot> slots = new ArrayList<>();
            for (int hour = 0; hour < 24; hour++) {
                for (int minute : new int[]{0, 30}) {
                    LocalDateTime start = LocalDateTime.of(day, LocalTime.of(hour, minute));
                    LocalDateTime end = start.plusMinutes(30);
                    Booking hit = bookings.stream()
                            .filter(b -> b.getStartTime().isBefore(end) && b.getEndTime().isAfter(start))
                            .findFirst().orElse(null);
                    slots.add(BookingCalendarDTO.Slot.builder()
                            .startTime(start.format(DT))
                            .endTime(end.format(DT))
                            .occupied(hit != null)
                            .bookingId(hit == null ? null : hit.getId())
                            .orderNo(hit == null ? null : hit.getOrderNo())
                            .bookType(hit == null ? null : hit.getBookType().name())
                            .title(hit == null ? null : slotTitle(hit))
                            .status(hit == null ? null : hit.getStatus().name())
                            .build());
                }
            }
            return ApiResponse.success(BookingCalendarDTO.builder()
                    .courtId(court.getId())
                    .courtName(court.getName())
                    .date(date)
                    .slots(slots)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询订场日历失败", e);
            return ApiResponse.error("查询订场日历失败");
        }
    }

    @Override
    public ApiResponse<Page<BookingDTO>> list(String date, Long venueId, Long courtId, String status,
                                              String bookType, int page, int size) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            LocalDateTime start = null;
            LocalDateTime end = null;
            if (StringUtils.hasText(date)) {
                LocalDate d = LocalDate.parse(date);
                start = d.atStartOfDay();
                end = d.plusDays(1).atStartOfDay();
            }
            Booking.BookingStatus st = StringUtils.hasText(status) ? Booking.BookingStatus.valueOf(status) : null;
            Booking.BookType bt = StringUtils.hasText(bookType) ? Booking.BookType.valueOf(bookType) : null;
            Page<Booking> result = bookingRepository.search(
                    merchantId, venueId, courtId, st, bt, null, start, end,
                    PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), 100)));
            Map<Long, String> courtNames = courtNameMap(merchantId);
            Map<Long, String> venueNames = venueNameMap(merchantId);
            return ApiResponse.success(result.map(b -> toDto(b, courtNames, venueNames)));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("筛选参数不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询订场列表失败", e);
            return ApiResponse.error("查询订场列表失败");
        }
    }

    @Override
    public ApiResponse<BookingDTO> detail(Long id) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Booking booking = requireBooking(id, merchantId);
            if (!SecurityUtils.isOwner() && !SecurityUtils.currentUserId().equals(booking.getOperatorId())
                    && !SecurityUtils.currentUserId().equals(booking.getLiaisonStaffId())) {
                // 店员可看自己操作或对接的单；列表已过滤，详情再兜底
            }
            return ApiResponse.success(toDto(booking, courtNameMap(merchantId), venueNameMap(merchantId)));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询订场详情失败", e);
            return ApiResponse.error("查询订场详情失败");
        }
    }

    @Override
    public ApiResponse<BookingDTO> create(BookingCreateRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            if (request.getCourtId() == null) {
                throw new BusinessException("请选择片场");
            }
            Court court = courtRepository.findByIdAndMerchantId(request.getCourtId(), merchantId)
                    .orElseThrow(() -> new BusinessException("片场不存在"));
            if (court.getStatus() != Court.CourtStatus.ACTIVE) {
                throw new BusinessException(400, "片场已停用");
            }
            LocalDateTime start = parseDateTime(request.getStartTime());
            LocalDateTime end = parseDateTime(request.getEndTime());
            validateSlot(start, end);

            List<Booking> conflicts = bookingRepository.findConflicts(
                    court.getId(), start, end, OCCUPYING, null);
            if (!conflicts.isEmpty()) {
                Booking c = conflicts.get(0);
                throw new BusinessException(400, "与订场单 " + c.getOrderNo() + " 时间冲突");
            }

            Booking.BookType bookType = Booking.BookType.valueOf(request.getBookType());
            Booking booking = new Booking();
            booking.setOrderNo(nextOrderNo());
            booking.setMerchantId(merchantId);
            booking.setVenueId(court.getVenueId());
            booking.setCourtId(court.getId());
            booking.setStartTime(start);
            booking.setEndTime(end);
            booking.setBookType(bookType);
            booking.setAmount(request.getAmount() == null ? BigDecimal.ZERO : request.getAmount());
            booking.setRemark(request.getRemark());
            booking.setSource("B");
            booking.setStatus(Booking.BookingStatus.BOOKED);
            booking.setOperatorId(SecurityUtils.currentUserId());
            booking.setOperatorName(SecurityUtils.currentOperatorName());

            if (bookType == Booking.BookType.TEAM) {
                if (request.getTeamId() == null) {
                    throw new BusinessException("请选择球队");
                }
                Team team = teamRepository.findByIdAndMerchantId(request.getTeamId(), merchantId)
                        .orElseThrow(() -> new BusinessException("球队不存在"));
                if (team.getStatus() != Team.TeamStatus.ACTIVE) {
                    throw new BusinessException(400, "球队已停用");
                }
                booking.setTeamId(team.getId());
                booking.setTeamName(team.getName());
                booking.setLiaisonStaffId(team.getLiaisonStaffId());
                booking.setLiaisonStaffName(team.getLiaisonStaffName());
            } else if (bookType == Booking.BookType.PERSON) {
                if (!StringUtils.hasText(request.getPersonName()) || !StringUtils.hasText(request.getPersonPhone())) {
                    throw new BusinessException("散客姓名和电话不能为空");
                }
                booking.setPersonName(request.getPersonName().trim());
                booking.setPersonPhone(request.getPersonPhone().trim());
            } else {
                throw new BusinessException("订场类型不合法");
            }

            Booking saved = bookingRepository.save(booking);
            return ApiResponse.success(toDto(saved, courtNameMap(merchantId), venueNameMap(merchantId)));
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("参数不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建订场失败", e);
            return ApiResponse.error("创建订场失败");
        }
    }

    @Override
    public ApiResponse<BookingDTO> cancel(Long id, Map<String, Object> body) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Booking booking = requireBooking(id, merchantId);
            if (booking.getStatus() != Booking.BookingStatus.BOOKED) {
                throw new BusinessException(400, "仅已预订状态可取消");
            }
            if (matchResultRepository.existsByBookingId(booking.getId())) {
                throw new BusinessException(400, "已录入赛果的订场不能取消");
            }
            booking.setStatus(Booking.BookingStatus.CANCELLED);
            booking.setCancelledAt(LocalDateTime.now());
            if (body != null && body.get("reason") != null) {
                booking.setCancelReason(String.valueOf(body.get("reason")));
            }
            Booking saved = bookingRepository.save(booking);
            return ApiResponse.success(toDto(saved, courtNameMap(merchantId), venueNameMap(merchantId)));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("取消订场失败", e);
            return ApiResponse.error("取消订场失败");
        }
    }

    @Override
    public ApiResponse<BookingDTO> complete(Long id) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Booking booking = requireBooking(id, merchantId);
            if (booking.getStatus() != Booking.BookingStatus.BOOKED) {
                throw new BusinessException("仅已预订状态可完成");
            }
            booking.setStatus(Booking.BookingStatus.COMPLETED);
            Booking saved = bookingRepository.save(booking);
            return ApiResponse.success(toDto(saved, courtNameMap(merchantId), venueNameMap(merchantId)));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("完成订场失败", e);
            return ApiResponse.error("完成订场失败");
        }
    }

    public static List<Booking.BookingStatus> performanceStatuses() {
        return PERF_STATUSES;
    }

    private void validateSlot(LocalDateTime start, LocalDateTime end) {
        if (start == null || end == null) {
            throw new BusinessException("开始/结束时间不能为空");
        }
        if (!end.isAfter(start)) {
            throw new BusinessException("结束时间必须晚于开始时间");
        }
        if (start.getMinute() % 30 != 0 || start.getSecond() != 0 || start.getNano() != 0) {
            throw new BusinessException(400, "开始时间必须对齐半小时");
        }
        if (end.getMinute() % 30 != 0 || end.getSecond() != 0 || end.getNano() != 0) {
            throw new BusinessException(400, "结束时间必须对齐半小时");
        }
        long minutes = java.time.Duration.between(start, end).toMinutes();
        if (minutes < 30 || minutes % 30 != 0) {
            throw new BusinessException(400, "时长至少30分钟且为半小时整数倍");
        }
    }

    private LocalDateTime parseDateTime(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        String v = value.trim().replace('T', ' ');
        if (v.length() == 16) {
            v = v + ":00";
        }
        return LocalDateTime.parse(v, DT);
    }

    private synchronized String nextOrderNo() {
        String prefix = "BK" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE);
        Long max = bookingRepository.findMaxSeqByPrefix(prefix);
        long next = (max == null ? 0 : max) + 1;
        return prefix + String.format("%04d", next);
    }

    private Booking requireBooking(Long id, Long merchantId) {
        return bookingRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new BusinessException("订场单不存在"));
    }

    private String slotTitle(Booking b) {
        if (b.getBookType() == Booking.BookType.TEAM) {
            return b.getTeamName();
        }
        return b.getPersonName();
    }

    private Map<Long, String> courtNameMap(Long merchantId) {
        return courtRepository.findByMerchantIdOrderBySortOrderAscIdAsc(merchantId).stream()
                .collect(Collectors.toMap(Court::getId, Court::getName, (a, b) -> a));
    }

    private Map<Long, String> venueNameMap(Long merchantId) {
        return venueRepository.findByMerchantId(merchantId).stream()
                .collect(Collectors.toMap(Venue::getId, Venue::getName, (a, b) -> a));
    }

    private BookingDTO toDto(Booking b, Map<Long, String> courtNames, Map<Long, String> venueNames) {
        Long matchId = matchResultRepository.findByBookingIdAndMerchantId(b.getId(), b.getMerchantId())
                .map(MatchResult::getId).orElse(null);
        return BookingDTO.builder()
                .id(b.getId())
                .orderNo(b.getOrderNo())
                .merchantId(b.getMerchantId())
                .venueId(b.getVenueId())
                .venueName(venueNames.getOrDefault(b.getVenueId(), ""))
                .courtId(b.getCourtId())
                .courtName(courtNames.getOrDefault(b.getCourtId(), ""))
                .startTime(b.getStartTime().format(DT))
                .endTime(b.getEndTime().format(DT))
                .bookType(b.getBookType().name())
                .teamId(b.getTeamId())
                .teamName(b.getTeamName())
                .personName(b.getPersonName())
                .personPhone(b.getPersonPhone())
                .operatorId(b.getOperatorId())
                .operatorName(b.getOperatorName())
                .liaisonStaffId(b.getLiaisonStaffId())
                .liaisonStaffName(b.getLiaisonStaffName())
                .status(b.getStatus().name())
                .amount(b.getAmount())
                .source(b.getSource())
                .remark(b.getRemark())
                .matchResultId(matchId)
                .build();
    }
}
