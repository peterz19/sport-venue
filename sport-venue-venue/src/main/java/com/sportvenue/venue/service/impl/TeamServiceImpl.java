package com.sportvenue.venue.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.booking.BookingDTO;
import com.sportvenue.venue.dto.team.*;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.service.TeamService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class TeamServiceImpl implements TeamService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private TeamAuditLogRepository teamAuditLogRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BookingRepository bookingRepository;
    @Autowired
    private MatchResultRepository matchResultRepository;
    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public ApiResponse<List<TeamDTO>> list() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            return ApiResponse.success(teamRepository.findByMerchantIdOrderByIdDesc(merchantId).stream()
                    .map(this::toDto).collect(Collectors.toList()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询球队失败", e);
            return ApiResponse.error("查询球队失败");
        }
    }

    @Override
    public ApiResponse<List<TeamDTO>> options() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            return ApiResponse.success(teamRepository
                    .findByMerchantIdAndStatusOrderByNameAsc(merchantId, Team.TeamStatus.ACTIVE).stream()
                    .map(this::toDto).collect(Collectors.toList()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询球队选项失败", e);
            return ApiResponse.error("查询球队选项失败");
        }
    }

    @Override
    public ApiResponse<TeamDetailDTO> detail(Long id) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Team team = requireTeam(id, merchantId);
            List<BookingDTO> recent = bookingRepository
                    .findTop10ByTeamIdAndMerchantIdOrderByStartTimeDesc(id, merchantId).stream()
                    .map(this::toBookingBrief).collect(Collectors.toList());
            Map<String, Object> summary = buildRecordSummary(merchantId, id);
            List<TeamAuditDTO> audits = teamAuditLogRepository
                    .findByTeamIdAndMerchantIdOrderByCreateTimeDesc(id, merchantId).stream()
                    .map(this::toAuditDto).collect(Collectors.toList());
            return ApiResponse.success(TeamDetailDTO.builder()
                    .team(toDto(team))
                    .recordSummary(summary)
                    .recentBookings(recent)
                    .audits(audits)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询球队详情失败", e);
            return ApiResponse.error("查询球队详情失败");
        }
    }

    @Override
    public ApiResponse<TeamDTO> create(TeamSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            validateBase(request);
            User liaison = requireStaff(request.getLiaisonStaffId(), merchantId);

            Team team = new Team();
            team.setMerchantId(merchantId);
            team.setName(request.getName().trim());
            team.setCaptainName(request.getCaptainName().trim());
            team.setPhone(request.getPhone().trim());
            team.setRemark(request.getRemark());
            team.setLiaisonStaffId(liaison.getId());
            team.setLiaisonStaffName(displayName(liaison));
            team.setStatus(Team.TeamStatus.ACTIVE);
            team.setCreateBy(SecurityUtils.currentUserId());
            Team saved = teamRepository.save(team);
            writeAudit(saved, "CREATE", null, snapshot(saved), null);
            return ApiResponse.success(toDto(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建球队失败", e);
            return ApiResponse.error("创建球队失败");
        }
    }

    @Override
    public ApiResponse<TeamDTO> update(Long id, TeamSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Team team = requireTeam(id, merchantId);
            validateBase(request);
            Map<String, Object> before = snapshot(team);
            team.setName(request.getName().trim());
            team.setCaptainName(request.getCaptainName().trim());
            team.setPhone(request.getPhone().trim());
            team.setRemark(request.getRemark());
            if (request.getLiaisonStaffId() != null
                    && !request.getLiaisonStaffId().equals(team.getLiaisonStaffId())) {
                User liaison = requireStaff(request.getLiaisonStaffId(), merchantId);
                team.setLiaisonStaffId(liaison.getId());
                team.setLiaisonStaffName(displayName(liaison));
            }
            team.setUpdateBy(SecurityUtils.currentUserId());
            Team saved = teamRepository.save(team);
            writeAudit(saved, "UPDATE", before, snapshot(saved), null);
            return ApiResponse.success(toDto(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新球队失败", e);
            return ApiResponse.error("更新球队失败");
        }
    }

    @Override
    public ApiResponse<TeamDTO> changeLiaison(Long id, TeamLiaisonRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Team team = requireTeam(id, merchantId);
            if (request.getLiaisonStaffId() == null) {
                throw new BusinessException("请选择对接员工");
            }
            if (request.getLiaisonStaffId().equals(team.getLiaisonStaffId())) {
                throw new BusinessException("对接员工未变化");
            }
            User liaison = requireStaff(request.getLiaisonStaffId(), merchantId);
            Map<String, Object> before = snapshot(team);
            team.setLiaisonStaffId(liaison.getId());
            team.setLiaisonStaffName(displayName(liaison));
            team.setUpdateBy(SecurityUtils.currentUserId());
            Team saved = teamRepository.save(team);
            writeAudit(saved, "CHANGE_LIAISON", before, snapshot(saved), request.getReason());
            return ApiResponse.success(toDto(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更换对接人失败", e);
            return ApiResponse.error("更换对接人失败");
        }
    }

    @Override
    public ApiResponse<Void> updateStatus(Long id, String status) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Team team = requireTeam(id, merchantId);
            Team.TeamStatus next = Team.TeamStatus.valueOf(status);
            Map<String, Object> before = snapshot(team);
            team.setStatus(next);
            team.setUpdateBy(SecurityUtils.currentUserId());
            Team saved = teamRepository.save(team);
            writeAudit(saved, next == Team.TeamStatus.ACTIVE ? "ENABLE" : "DISABLE", before, snapshot(saved), null);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("状态不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新球队状态失败", e);
            return ApiResponse.error("更新球队状态失败");
        }
    }

    @Override
    public ApiResponse<List<TeamAuditDTO>> audits(Long id) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            requireTeam(id, merchantId);
            return ApiResponse.success(teamAuditLogRepository
                    .findByTeamIdAndMerchantIdOrderByCreateTimeDesc(id, merchantId).stream()
                    .map(this::toAuditDto).collect(Collectors.toList()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询球队审计失败", e);
            return ApiResponse.error("查询球队审计失败");
        }
    }

    private Map<String, Object> buildRecordSummary(Long merchantId, Long teamId) {
        List<MatchResult> matches = matchResultRepository.findByTeam(merchantId, teamId);
        int win = 0, draw = 0, loss = 0, pf = 0, pa = 0;
        for (MatchResult m : matches) {
            boolean home = teamId.equals(m.getHomeTeamId());
            int mine = home ? m.getHomeScore() : m.getAwayScore();
            int theirs = home ? m.getAwayScore() : m.getHomeScore();
            pf += mine;
            pa += theirs;
            if (mine > theirs) win++;
            else if (mine < theirs) loss++;
            else draw++;
        }
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("played", matches.size());
        map.put("win", win);
        map.put("draw", draw);
        map.put("loss", loss);
        map.put("pointsFor", pf);
        map.put("pointsAgainst", pa);
        return map;
    }

    private void validateBase(TeamSaveRequest request) {
        if (!StringUtils.hasText(request.getName())) throw new BusinessException("队名不能为空");
        if (!StringUtils.hasText(request.getCaptainName())) throw new BusinessException("队长不能为空");
        if (!StringUtils.hasText(request.getPhone())) throw new BusinessException("电话不能为空");
        if (request.getLiaisonStaffId() == null) throw new BusinessException("对接员工不能为空");
    }

    private Team requireTeam(Long id, Long merchantId) {
        return teamRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new BusinessException("球队不存在"));
    }

    private User requireStaff(Long staffId, Long merchantId) {
        User user = userRepository.findByIdAndMerchantId(staffId, merchantId)
                .orElseThrow(() -> new BusinessException("对接员工不存在"));
        if (user.getUserType() != User.UserType.B_MERCHANT && user.getUserType() != User.UserType.B_STAFF) {
            throw new BusinessException("对接员工角色不合法");
        }
        if (user.getStatus() != User.UserStatus.ACTIVE) {
            throw new BusinessException("对接员工已停用");
        }
        return user;
    }

    private void writeAudit(Team team, String action, Map<String, Object> before,
                            Map<String, Object> after, String reason) {
        try {
            TeamAuditLog logEntity = new TeamAuditLog();
            logEntity.setTeamId(team.getId());
            logEntity.setMerchantId(team.getMerchantId());
            logEntity.setAction(action);
            logEntity.setBeforeJson(before == null ? null : objectMapper.writeValueAsString(before));
            logEntity.setAfterJson(after == null ? null : objectMapper.writeValueAsString(after));
            logEntity.setReason(reason);
            logEntity.setOperatorId(SecurityUtils.currentUserId());
            logEntity.setOperatorName(SecurityUtils.currentOperatorName());
            teamAuditLogRepository.save(logEntity);
        } catch (Exception e) {
            throw new BusinessException("写入审计失败");
        }
    }

    private Map<String, Object> snapshot(Team team) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("name", team.getName());
        m.put("captainName", team.getCaptainName());
        m.put("phone", team.getPhone());
        m.put("remark", team.getRemark());
        m.put("liaisonStaffId", team.getLiaisonStaffId());
        m.put("liaisonStaffName", team.getLiaisonStaffName());
        m.put("status", team.getStatus() == null ? null : team.getStatus().name());
        return m;
    }

    private String displayName(User user) {
        return StringUtils.hasText(user.getRealName()) ? user.getRealName() : user.getUsername();
    }

    private TeamDTO toDto(Team t) {
        return TeamDTO.builder()
                .id(t.getId())
                .merchantId(t.getMerchantId())
                .name(t.getName())
                .captainName(t.getCaptainName())
                .phone(t.getPhone())
                .remark(t.getRemark())
                .liaisonStaffId(t.getLiaisonStaffId())
                .liaisonStaffName(t.getLiaisonStaffName())
                .status(t.getStatus().name())
                .build();
    }

    private TeamAuditDTO toAuditDto(TeamAuditLog log) {
        return TeamAuditDTO.builder()
                .id(log.getId())
                .teamId(log.getTeamId())
                .action(log.getAction())
                .beforeJson(log.getBeforeJson())
                .afterJson(log.getAfterJson())
                .reason(log.getReason())
                .operatorId(log.getOperatorId())
                .operatorName(log.getOperatorName())
                .createTime(log.getCreateTime() == null ? null : log.getCreateTime().format(DT))
                .build();
    }

    private BookingDTO toBookingBrief(Booking b) {
        return BookingDTO.builder()
                .id(b.getId())
                .orderNo(b.getOrderNo())
                .courtId(b.getCourtId())
                .startTime(b.getStartTime().format(DT))
                .endTime(b.getEndTime().format(DT))
                .bookType(b.getBookType().name())
                .status(b.getStatus().name())
                .operatorName(b.getOperatorName())
                .liaisonStaffName(b.getLiaisonStaffName())
                .amount(b.getAmount())
                .build();
    }
}
