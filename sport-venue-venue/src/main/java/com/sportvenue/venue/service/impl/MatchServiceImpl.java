package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.match.MatchResultDTO;
import com.sportvenue.venue.dto.match.MatchSaveRequest;
import com.sportvenue.venue.dto.match.TeamRankingDTO;
import com.sportvenue.venue.entity.Booking;
import com.sportvenue.venue.entity.MatchResult;
import com.sportvenue.venue.entity.Team;
import com.sportvenue.venue.repository.BookingRepository;
import com.sportvenue.venue.repository.MatchResultRepository;
import com.sportvenue.venue.repository.TeamRepository;
import com.sportvenue.venue.service.MatchService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MatchServiceImpl implements MatchService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private MatchResultRepository matchResultRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private TeamRepository teamRepository;

    @Override
    public ApiResponse<List<MatchResultDTO>> list() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Map<Long, String> teamNames = teamNameMap(merchantId);
            Map<Long, String> bookingNos = new HashMap<>();
            List<MatchResultDTO> list = matchResultRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId)
                    .stream()
                    .map(m -> toDto(m, teamNames, bookingNos))
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询赛果失败", e);
            return ApiResponse.error("查询赛果失败");
        }
    }

    @Override
    public ApiResponse<MatchResultDTO> create(MatchSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Booking booking = requireBooking(request.getBookingId(), merchantId);
            if (booking.getStatus() == Booking.BookingStatus.CANCELLED
                    || booking.getStatus() == Booking.BookingStatus.EXPIRED) {
                throw new BusinessException(400, "已取消/过期订场不能录赛果");
            }
            if (matchResultRepository.existsByBookingId(booking.getId())) {
                throw new BusinessException(400, "该订场已有赛果，请使用修正接口");
            }
            Team home = requireActiveTeam(request.getHomeTeamId(), merchantId);
            Team away = requireActiveTeam(request.getAwayTeamId(), merchantId);
            if (home.getId().equals(away.getId())) {
                throw new BusinessException(400, "主客队不能相同");
            }
            if (request.getHomeScore() == null || request.getAwayScore() == null) {
                throw new BusinessException(400, "比分不能为空");
            }
            if (request.getHomeScore() < 0 || request.getAwayScore() < 0) {
                throw new BusinessException(400, "比分不能为负");
            }

            MatchResult result = new MatchResult();
            result.setBookingId(booking.getId());
            result.setMerchantId(merchantId);
            result.setHomeTeamId(home.getId());
            result.setAwayTeamId(away.getId());
            result.setHomeScore(request.getHomeScore());
            result.setAwayScore(request.getAwayScore());
            result.setResult(calcOutcome(request.getHomeScore(), request.getAwayScore()));
            result.setOperatorId(SecurityUtils.currentUserId());
            result.setOperatorName(SecurityUtils.currentOperatorName());
            result.setRemark(request.getRemark());
            MatchResult saved = matchResultRepository.save(result);

            if (booking.getStatus() == Booking.BookingStatus.BOOKED) {
                booking.setStatus(Booking.BookingStatus.COMPLETED);
                bookingRepository.save(booking);
            }

            Map<Long, String> teamNames = Map.of(home.getId(), home.getName(), away.getId(), away.getName());
            return ApiResponse.success(toDto(saved, teamNames, Map.of(booking.getId(), booking.getOrderNo())));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("录入赛果失败", e);
            return ApiResponse.error("录入赛果失败");
        }
    }

    @Override
    public ApiResponse<MatchResultDTO> update(Long id, MatchSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            MatchResult result = matchResultRepository.findByIdAndMerchantId(id, merchantId)
                    .orElseThrow(() -> new BusinessException("赛果不存在"));
            Team home = requireActiveTeam(
                    request.getHomeTeamId() == null ? result.getHomeTeamId() : request.getHomeTeamId(), merchantId);
            Team away = requireActiveTeam(
                    request.getAwayTeamId() == null ? result.getAwayTeamId() : request.getAwayTeamId(), merchantId);
            if (home.getId().equals(away.getId())) {
                throw new BusinessException(400, "主客队不能相同");
            }
            Integer hs = request.getHomeScore() == null ? result.getHomeScore() : request.getHomeScore();
            Integer as = request.getAwayScore() == null ? result.getAwayScore() : request.getAwayScore();
            if (hs < 0 || as < 0) {
                throw new BusinessException(400, "比分不能为负");
            }
            result.setHomeTeamId(home.getId());
            result.setAwayTeamId(away.getId());
            result.setHomeScore(hs);
            result.setAwayScore(as);
            result.setResult(calcOutcome(hs, as));
            if (request.getRemark() != null) {
                result.setRemark(request.getRemark());
            }
            result.setOperatorId(SecurityUtils.currentUserId());
            result.setOperatorName(SecurityUtils.currentOperatorName());
            MatchResult saved = matchResultRepository.save(result);
            Map<Long, String> teamNames = Map.of(home.getId(), home.getName(), away.getId(), away.getName());
            return ApiResponse.success(toDto(saved, teamNames, new HashMap<>()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("修正赛果失败", e);
            return ApiResponse.error("修正赛果失败");
        }
    }

    @Override
    public ApiResponse<List<TeamRankingDTO>> ranking() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            List<Team> teams = teamRepository.findByMerchantIdAndStatusOrderByNameAsc(merchantId, Team.TeamStatus.ACTIVE);
            List<MatchResult> matches = matchResultRepository.findByMerchantIdOrderByCreateTimeDesc(merchantId);

            Map<Long, Agg> map = new HashMap<>();
            for (Team t : teams) {
                map.put(t.getId(), new Agg(t.getId(), t.getName()));
            }
            for (MatchResult m : matches) {
                applyMatch(map, m.getHomeTeamId(), m.getHomeScore(), m.getAwayScore());
                applyMatch(map, m.getAwayTeamId(), m.getAwayScore(), m.getHomeScore());
            }

            List<TeamRankingDTO> list = map.values().stream()
                    .map(a -> TeamRankingDTO.builder()
                            .teamId(a.teamId)
                            .teamName(a.teamName)
                            .played(a.played)
                            .win(a.win)
                            .draw(a.draw)
                            .loss(a.loss)
                            .pointsFor(a.pf)
                            .pointsAgainst(a.pa)
                            .pointDiff(a.pf - a.pa)
                            .rankingPoints(a.win * 3 + a.draw)
                            .build())
                    .sorted((x, y) -> {
                        int c = Integer.compare(y.getRankingPoints(), x.getRankingPoints());
                        if (c != 0) return c;
                        c = Integer.compare(y.getPointDiff(), x.getPointDiff());
                        if (c != 0) return c;
                        return Integer.compare(y.getPointsFor(), x.getPointsFor());
                    })
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询球队排行失败", e);
            return ApiResponse.error("查询球队排行失败");
        }
    }

    private void applyMatch(Map<Long, Agg> map, Long teamId, int mine, int theirs) {
        Agg a = map.get(teamId);
        if (a == null) {
            return;
        }
        a.played++;
        a.pf += mine;
        a.pa += theirs;
        if (mine > theirs) a.win++;
        else if (mine < theirs) a.loss++;
        else a.draw++;
    }

    private MatchResult.MatchOutcome calcOutcome(int home, int away) {
        if (home > away) return MatchResult.MatchOutcome.HOME_WIN;
        if (home < away) return MatchResult.MatchOutcome.AWAY_WIN;
        return MatchResult.MatchOutcome.DRAW;
    }

    private Booking requireBooking(Long bookingId, Long merchantId) {
        if (bookingId == null) {
            throw new BusinessException("请关联订场单");
        }
        return bookingRepository.findByIdAndMerchantId(bookingId, merchantId)
                .orElseThrow(() -> new BusinessException("订场单不存在"));
    }

    private Team requireActiveTeam(Long teamId, Long merchantId) {
        if (teamId == null) {
            throw new BusinessException("请选择球队");
        }
        Team team = teamRepository.findByIdAndMerchantId(teamId, merchantId)
                .orElseThrow(() -> new BusinessException("球队不存在"));
        if (team.getStatus() != Team.TeamStatus.ACTIVE) {
            throw new BusinessException("球队已停用: " + team.getName());
        }
        return team;
    }

    private Map<Long, String> teamNameMap(Long merchantId) {
        return teamRepository.findByMerchantIdOrderByIdDesc(merchantId).stream()
                .collect(Collectors.toMap(Team::getId, Team::getName, (a, b) -> a));
    }

    private MatchResultDTO toDto(MatchResult m, Map<Long, String> teamNames, Map<Long, String> bookingNos) {
        String orderNo = bookingNos.get(m.getBookingId());
        if (orderNo == null) {
            orderNo = bookingRepository.findById(m.getBookingId()).map(Booking::getOrderNo).orElse(null);
        }
        return MatchResultDTO.builder()
                .id(m.getId())
                .bookingId(m.getBookingId())
                .bookingOrderNo(orderNo)
                .homeTeamId(m.getHomeTeamId())
                .homeTeamName(teamNames.getOrDefault(m.getHomeTeamId(), ""))
                .awayTeamId(m.getAwayTeamId())
                .awayTeamName(teamNames.getOrDefault(m.getAwayTeamId(), ""))
                .homeScore(m.getHomeScore())
                .awayScore(m.getAwayScore())
                .result(m.getResult().name())
                .operatorId(m.getOperatorId())
                .operatorName(m.getOperatorName())
                .remark(m.getRemark())
                .createTime(m.getCreateTime() == null ? null : m.getCreateTime().format(DT))
                .build();
    }

    private static class Agg {
        private final Long teamId;
        private final String teamName;
        private int played;
        private int win;
        private int draw;
        private int loss;
        private int pf;
        private int pa;

        private Agg(Long teamId, String teamName) {
            this.teamId = teamId;
            this.teamName = teamName;
        }
    }
}
