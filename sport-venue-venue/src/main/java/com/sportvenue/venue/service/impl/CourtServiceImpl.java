package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.court.CourtDTO;
import com.sportvenue.venue.dto.court.CourtSaveRequest;
import com.sportvenue.venue.entity.Court;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.repository.CourtRepository;
import com.sportvenue.venue.repository.VenueRepository;
import com.sportvenue.venue.service.CourtService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class CourtServiceImpl implements CourtService {

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private VenueRepository venueRepository;

    @Override
    public ApiResponse<List<CourtDTO>> list(Long venueId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            List<Court> courts = venueId == null
                    ? courtRepository.findByMerchantIdOrderBySortOrderAscIdAsc(merchantId)
                    : courtRepository.findByMerchantIdAndVenueIdOrderBySortOrderAscIdAsc(merchantId, venueId);
            Map<Long, String> venueNames = venueNameMap(merchantId);
            return ApiResponse.success(courts.stream().map(c -> toDto(c, venueNames)).collect(Collectors.toList()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询片场失败", e);
            return ApiResponse.error("查询片场失败");
        }
    }

    @Override
    public ApiResponse<List<CourtDTO>> options(Long venueId) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            List<Court> courts = venueId == null
                    ? courtRepository.findByMerchantIdAndStatusOrderBySortOrderAscIdAsc(merchantId, Court.CourtStatus.ACTIVE)
                    : courtRepository.findByMerchantIdAndVenueIdAndStatusOrderBySortOrderAscIdAsc(
                    merchantId, venueId, Court.CourtStatus.ACTIVE);
            Map<Long, String> venueNames = venueNameMap(merchantId);
            return ApiResponse.success(courts.stream().map(c -> toDto(c, venueNames)).collect(Collectors.toList()));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询片场选项失败", e);
            return ApiResponse.error("查询片场选项失败");
        }
    }

    @Override
    public ApiResponse<CourtDTO> create(CourtSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Venue venue = requireVenue(request.getVenueId(), merchantId);
            if (!StringUtils.hasText(request.getName())) {
                throw new BusinessException("片场名称不能为空");
            }
            Court court = new Court();
            court.setMerchantId(merchantId);
            court.setVenueId(venue.getId());
            apply(court, request);
            court.setStatus(Court.CourtStatus.ACTIVE);
            court.setCreateBy(SecurityUtils.currentUserId());
            Court saved = courtRepository.save(court);
            return ApiResponse.success(toDto(saved, Map.of(venue.getId(), venue.getName())));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建片场失败", e);
            return ApiResponse.error("创建片场失败");
        }
    }

    @Override
    public ApiResponse<CourtDTO> update(Long id, CourtSaveRequest request) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Court court = courtRepository.findByIdAndMerchantId(id, merchantId)
                    .orElseThrow(() -> new BusinessException("片场不存在"));
            if (request.getVenueId() != null && !request.getVenueId().equals(court.getVenueId())) {
                Venue venue = requireVenue(request.getVenueId(), merchantId);
                court.setVenueId(venue.getId());
            }
            apply(court, request);
            court.setUpdateBy(SecurityUtils.currentUserId());
            Court saved = courtRepository.save(court);
            Map<Long, String> names = venueNameMap(merchantId);
            return ApiResponse.success(toDto(saved, names));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新片场失败", e);
            return ApiResponse.error("更新片场失败");
        }
    }

    @Override
    public ApiResponse<Void> updateStatus(Long id, String status) {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            Court court = courtRepository.findByIdAndMerchantId(id, merchantId)
                    .orElseThrow(() -> new BusinessException("片场不存在"));
            court.setStatus(Court.CourtStatus.valueOf(status));
            court.setUpdateBy(SecurityUtils.currentUserId());
            courtRepository.save(court);
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error("状态不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新片场状态失败", e);
            return ApiResponse.error("更新片场状态失败");
        }
    }

    private void apply(Court court, CourtSaveRequest request) {
        if (StringUtils.hasText(request.getName())) {
            court.setName(request.getName().trim());
        }
        court.setCode(StringUtils.hasText(request.getCode()) ? request.getCode().trim() : null);
        if (StringUtils.hasText(request.getCourtType())) {
            court.setCourtType(Court.CourtType.valueOf(request.getCourtType()));
        }
        if (request.getSortOrder() != null) {
            court.setSortOrder(request.getSortOrder());
        }
        court.setRemark(request.getRemark());
        if (StringUtils.hasText(request.getStatus())) {
            court.setStatus(Court.CourtStatus.valueOf(request.getStatus()));
        }
    }

    private Venue requireVenue(Long venueId, Long merchantId) {
        if (venueId == null) {
            throw new BusinessException("请选择场馆");
        }
        Venue venue = venueRepository.findById(venueId)
                .orElseThrow(() -> new BusinessException("场馆不存在"));
        if (!merchantId.equals(venue.getMerchantId())) {
            throw new BusinessException("场馆不属于当前商户");
        }
        return venue;
    }

    private Map<Long, String> venueNameMap(Long merchantId) {
        return venueRepository.findByMerchantId(merchantId).stream()
                .collect(Collectors.toMap(Venue::getId, Venue::getName, (a, b) -> a));
    }

    private CourtDTO toDto(Court c, Map<Long, String> venueNames) {
        return CourtDTO.builder()
                .id(c.getId())
                .merchantId(c.getMerchantId())
                .venueId(c.getVenueId())
                .venueName(venueNames.getOrDefault(c.getVenueId(), ""))
                .name(c.getName())
                .code(c.getCode())
                .courtType(c.getCourtType().name())
                .status(c.getStatus().name())
                .sortOrder(c.getSortOrder())
                .remark(c.getRemark())
                .build();
    }
}
