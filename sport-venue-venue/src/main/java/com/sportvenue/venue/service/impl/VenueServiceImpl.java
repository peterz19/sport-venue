package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.VenueDTO;
import com.sportvenue.venue.dto.VenueQueryDTO;
import com.sportvenue.venue.entity.Venue;
import com.sportvenue.venue.repository.VenueRepository;
import com.sportvenue.venue.service.VenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 场馆服务实现类
 */
@Slf4j
@Service
@Transactional
public class VenueServiceImpl implements VenueService {
    
    @Autowired
    private VenueRepository venueRepository;
    
    @Override
    public ApiResponse<VenueDTO> createVenue(Venue venue) {
        try {
            // 参数校验
            validateVenue(venue);
            
            // 检查场馆名称是否重复
            if (venueRepository.findByNameAndMerchantId(venue.getName(), venue.getMerchantId()).isPresent()) {
                throw new BusinessException("场馆名称已存在");
            }
            
            // 设置默认值
            venue.setCreateTime(LocalDateTime.now());
            venue.setUpdateTime(LocalDateTime.now());
            venue.setCurrentOccupancy(0);
            venue.setRating(BigDecimal.ZERO);
            venue.setRatingCount(0);
            
            // 保存场馆
            Venue savedVenue = venueRepository.save(venue);
            
            log.info("创建场馆成功，场馆ID：{}，场馆名称：{}", savedVenue.getId(), savedVenue.getName());
            
            return ApiResponse.success(VenueDTO.fromEntity(savedVenue));
        } catch (BusinessException e) {
            log.error("创建场馆失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("创建场馆异常：", e);
            return ApiResponse.error("创建场馆失败");
        }
    }
    
    @Override
    public ApiResponse<VenueDTO> updateVenue(Long id, Venue venue) {
        try {
            // 检查场馆是否存在
            Venue existingVenue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            // 参数校验
            validateVenue(venue);
            
            // 检查场馆名称是否重复（排除自己）
            Optional<Venue> duplicateVenue = venueRepository.findByNameAndMerchantId(venue.getName(), venue.getMerchantId());
            if (duplicateVenue.isPresent() && !duplicateVenue.get().getId().equals(id)) {
                throw new BusinessException("场馆名称已存在");
            }
            
            // 更新场馆信息
            existingVenue.setName(venue.getName());
            existingVenue.setDescription(venue.getDescription());
            existingVenue.setType(venue.getType());
            existingVenue.setSpaceType(venue.getSpaceType());
            existingVenue.setChargeType(venue.getChargeType());
            existingVenue.setAddress(venue.getAddress());
            existingVenue.setLongitude(venue.getLongitude());
            existingVenue.setLatitude(venue.getLatitude());
            existingVenue.setPhone(venue.getPhone());
            existingVenue.setOpenTime(venue.getOpenTime());
            existingVenue.setCloseTime(venue.getCloseTime());
            existingVenue.setCapacity(venue.getCapacity());
            existingVenue.setArea(venue.getArea());
            existingVenue.setFacilities(venue.getFacilities());
            existingVenue.setReservationEnabled(venue.getReservationEnabled());
            existingVenue.setCheckInEnabled(venue.getCheckInEnabled());
            existingVenue.setPointsEnabled(venue.getPointsEnabled());
            existingVenue.setUpdateTime(LocalDateTime.now());
            
            Venue updatedVenue = venueRepository.save(existingVenue);
            
            log.info("更新场馆成功，场馆ID：{}，场馆名称：{}", updatedVenue.getId(), updatedVenue.getName());
            
            return ApiResponse.success(VenueDTO.fromEntity(updatedVenue));
        } catch (BusinessException e) {
            log.error("更新场馆失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新场馆异常：", e);
            return ApiResponse.error("更新场馆失败");
        }
    }
    
    @Override
    public ApiResponse<Void> deleteVenue(Long id) {
        try {
            // 检查场馆是否存在
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            // 检查场馆是否可以删除（比如有预约记录等）
            // TODO: 添加业务逻辑检查
            
            venueRepository.deleteById(id);
            
            log.info("删除场馆成功，场馆ID：{}，场馆名称：{}", id, venue.getName());
            
            return ApiResponse.success();
        } catch (BusinessException e) {
            log.error("删除场馆失败：{}", e.getMessage());
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("删除场馆异常：", e);
            return ApiResponse.error("删除场馆失败");
        }
    }
    
    @Override
    public ApiResponse<VenueDTO> getVenueById(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            return ApiResponse.success(VenueDTO.fromEntity(venue));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("查询场馆异常：", e);
            return ApiResponse.error("查询场馆失败");
        }
    }
    
    @Override
    public ApiResponse<VenueDTO> getVenueByNo(String venueNo) {
        try {
            // TODO: 实现根据编号查询
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("查询场馆异常：", e);
            return ApiResponse.error("查询场馆失败");
        }
    }
    
    @Override
    public ApiResponse<Page<VenueDTO>> getVenueList(VenueQueryDTO queryDTO) {
        try {
            // 构建分页和排序
            String sortBy = queryDTO.getSortBy() != null ? queryDTO.getSortBy() : "createTime";
            String sortDirection = queryDTO.getSortDirection() != null ? queryDTO.getSortDirection() : "DESC";
            
            Sort sort;
            try {
                sort = Sort.by(Sort.Direction.fromString(sortDirection), sortBy);
            } catch (IllegalArgumentException e) {
                log.warn("无效的排序方向：{}，使用默认排序", sortDirection);
                sort = Sort.by(Sort.Direction.DESC, "createTime");
            }
            
            Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
            
            // 根据查询条件获取场馆列表
            Page<Venue> venuePage;
            
            if (queryDTO.getMerchantId() != null) {
                venuePage = venueRepository.findByMerchantId(queryDTO.getMerchantId(), pageable);
            } else if (queryDTO.getType() != null) {
                List<Venue> venues = venueRepository.findByType(queryDTO.getType());
                venuePage = createPageFromList(venues, pageable);
            } else if (queryDTO.getSpaceType() != null) {
                List<Venue> venues = venueRepository.findBySpaceType(queryDTO.getSpaceType());
                venuePage = createPageFromList(venues, pageable);
            } else if (queryDTO.getChargeType() != null) {
                List<Venue> venues = venueRepository.findByChargeType(queryDTO.getChargeType());
                venuePage = createPageFromList(venues, pageable);
            } else if (queryDTO.getStatus() != null) {
                List<Venue> venues = venueRepository.findByStatus(queryDTO.getStatus());
                venuePage = createPageFromList(venues, pageable);
            } else {
                // 默认查询所有活跃场馆
                List<Venue> venues = venueRepository.findByStatus(Venue.VenueStatus.ACTIVE);
                venuePage = createPageFromList(venues, pageable);
            }
            
            // 转换为DTO
            Page<VenueDTO> dtoPage = venuePage.map(VenueDTO::fromEntity);
            
            return ApiResponse.success(dtoPage);
        } catch (Exception e) {
            log.error("查询场馆列表异常：", e);
            return ApiResponse.error("查询场馆列表失败");
        }
    }
    
    @Override
    public ApiResponse<List<VenueDTO>> getVenuesByMerchant(Long merchantId) {
        try {
            List<Venue> venues = venueRepository.findByMerchantId(merchantId);
            List<VenueDTO> dtoList = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("查询商户场馆列表异常：", e);
            return ApiResponse.error("查询商户场馆列表失败");
        }
    }
    
    @Override
    public ApiResponse<List<VenueDTO>> getVenuesByType(Venue.VenueType type) {
        try {
            List<Venue> venues = venueRepository.findByType(type);
            List<VenueDTO> dtoList = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("查询场馆类型列表异常：", e);
            return ApiResponse.error("查询场馆类型列表失败");
        }
    }
    
    @Override
    public ApiResponse<List<VenueDTO>> getVenuesByStatus(Venue.VenueStatus status) {
        try {
            List<Venue> venues = venueRepository.findByStatus(status);
            List<VenueDTO> dtoList = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("查询场馆状态列表异常：", e);
            return ApiResponse.error("查询场馆状态列表失败");
        }
    }
    
    @Override
    public ApiResponse<List<VenueDTO>> searchNearbyVenues(BigDecimal longitude, BigDecimal latitude, BigDecimal radius) {
        try {
            // 计算搜索范围
            BigDecimal minLng = longitude.subtract(radius);
            BigDecimal maxLng = longitude.add(radius);
            BigDecimal minLat = latitude.subtract(radius);
            BigDecimal maxLat = latitude.add(radius);
            
            List<Venue> venues = venueRepository.findByLocationRange(minLng, maxLng, minLat, maxLat);
            List<VenueDTO> dtoList = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("搜索附近场馆异常：", e);
            return ApiResponse.error("搜索附近场馆失败");
        }
    }
    
    @Override
    public ApiResponse<List<VenueDTO>> getPopularVenues(Integer limit) {
        try {
            // 根据评分和预约次数排序，获取热门场馆
            List<Venue> venues = venueRepository.findByStatusOrderByRatingDescRatingCountDesc(Venue.VenueStatus.ACTIVE);
            
            // 限制返回数量
            if (limit != null && limit > 0) {
                venues = venues.stream().limit(limit).collect(Collectors.toList());
            }
            
            List<VenueDTO> venueDTOs = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(venueDTOs);
        } catch (Exception e) {
            log.error("获取热门场馆异常：", e);
            return ApiResponse.error("获取热门场馆失败");
        }
    }

    @Override
    public ApiResponse<List<VenueDTO>> searchVenuesByName(String name) {
        try {
            if (!StringUtils.hasText(name)) {
                return ApiResponse.error("搜索关键词不能为空");
            }
            
            List<Venue> venues = venueRepository.findByNameContainingAndStatus(name, Venue.VenueStatus.ACTIVE);
            
            List<VenueDTO> venueDTOs = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(venueDTOs);
        } catch (Exception e) {
            log.error("搜索场馆异常：", e);
            return ApiResponse.error("搜索场馆失败");
        }
    }

    @Override
    public ApiResponse<List<VenueDTO>> getRecommendedVenues(BigDecimal longitude, BigDecimal latitude, Integer limit) {
        try {
            List<Venue> venues = new ArrayList<>();
            
            if (longitude != null && latitude != null) {
                // 基于位置推荐：优先推荐附近的场馆
                venues = venueRepository.findByStatusOrderByDistanceAsc(Venue.VenueStatus.ACTIVE, longitude, latitude);
            } else {
                // 基于热度推荐：推荐评分高的场馆
                venues = venueRepository.findByStatusOrderByRatingDesc(Venue.VenueStatus.ACTIVE);
            }
            
            // 限制返回数量
            if (limit != null && limit > 0) {
                venues = venues.stream().limit(limit).collect(Collectors.toList());
            }
            
            List<VenueDTO> venueDTOs = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(venueDTOs);
        } catch (Exception e) {
            log.error("获取推荐场馆异常：", e);
            return ApiResponse.error("获取推荐场馆失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getVenueRealtimeInfo(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            Map<String, Object> realtimeInfo = new HashMap<>();
            realtimeInfo.put("venueId", venue.getId());
            realtimeInfo.put("venueName", venue.getName());
            realtimeInfo.put("currentOccupancy", venue.getCurrentOccupancy());
            realtimeInfo.put("capacity", venue.getCapacity());
            realtimeInfo.put("occupancyRate", venue.getCapacity() > 0 ? 
                (double) venue.getCurrentOccupancy() / venue.getCapacity() : 0.0);
            realtimeInfo.put("isOpen", isVenueOpen(venue));
            realtimeInfo.put("availableSlots", venue.getCapacity() - venue.getCurrentOccupancy());
            realtimeInfo.put("lastUpdateTime", venue.getUpdateTime());
            
            return ApiResponse.success(realtimeInfo);
        } catch (Exception e) {
            log.error("获取场馆实时信息异常：", e);
            return ApiResponse.error("获取场馆实时信息失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getVenueRealtimeData(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            Map<String, Object> realtimeData = new HashMap<>();
            realtimeData.put("venueId", venue.getId());
            realtimeData.put("venueName", venue.getName());
            realtimeData.put("currentOccupancy", venue.getCurrentOccupancy());
            realtimeData.put("capacity", venue.getCapacity());
            realtimeData.put("occupancyRate", venue.getCapacity() > 0 ? 
                (double) venue.getCurrentOccupancy() / venue.getCapacity() : 0.0);
            realtimeData.put("isOpen", isVenueOpen(venue));
            realtimeData.put("availableSlots", venue.getCapacity() - venue.getCurrentOccupancy());
            realtimeData.put("rating", venue.getRating());
            realtimeData.put("ratingCount", venue.getRatingCount());
            realtimeData.put("status", venue.getStatus());
            realtimeData.put("lastUpdateTime", venue.getUpdateTime());
            
            // TODO: 添加更多实时数据，如当前预约数、今日收入等
            
            return ApiResponse.success(realtimeData);
        } catch (Exception e) {
            log.error("获取场馆实时数据异常：", e);
            return ApiResponse.error("获取场馆实时数据失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getVenueReservationStats(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            Map<String, Object> reservationStats = new HashMap<>();
            reservationStats.put("venueId", venue.getId());
            reservationStats.put("venueName", venue.getName());
            
            // TODO: 从预约表查询统计数据
            reservationStats.put("todayReservations", 0);
            reservationStats.put("weekReservations", 0);
            reservationStats.put("monthReservations", 0);
            reservationStats.put("totalReservations", 0);
            reservationStats.put("pendingReservations", 0);
            reservationStats.put("confirmedReservations", 0);
            reservationStats.put("cancelledReservations", 0);
            
            return ApiResponse.success(reservationStats);
        } catch (Exception e) {
            log.error("获取场馆预约统计异常：", e);
            return ApiResponse.error("获取场馆预约统计失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getVenueCheckInStats(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            Map<String, Object> checkInStats = new HashMap<>();
            checkInStats.put("venueId", venue.getId());
            checkInStats.put("venueName", venue.getName());
            
            // TODO: 从打卡表查询统计数据
            checkInStats.put("todayCheckIns", 0);
            checkInStats.put("weekCheckIns", 0);
            checkInStats.put("monthCheckIns", 0);
            checkInStats.put("totalCheckIns", 0);
            checkInStats.put("currentCheckIns", venue.getCurrentOccupancy());
            checkInStats.put("peakCheckIns", 0);
            checkInStats.put("averageCheckIns", 0.0);
            
            return ApiResponse.success(checkInStats);
        } catch (Exception e) {
            log.error("获取场馆打卡统计异常：", e);
            return ApiResponse.error("获取场馆打卡统计失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getVenueRevenueStats(Long id) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            Map<String, Object> revenueStats = new HashMap<>();
            revenueStats.put("venueId", venue.getId());
            revenueStats.put("venueName", venue.getName());
            
            // TODO: 从收入表查询统计数据
            revenueStats.put("todayRevenue", BigDecimal.ZERO);
            revenueStats.put("weekRevenue", BigDecimal.ZERO);
            revenueStats.put("monthRevenue", BigDecimal.ZERO);
            revenueStats.put("totalRevenue", BigDecimal.ZERO);
            revenueStats.put("averageRevenue", BigDecimal.ZERO);
            revenueStats.put("peakRevenue", BigDecimal.ZERO);
            
            return ApiResponse.success(revenueStats);
        } catch (Exception e) {
            log.error("获取场馆收入统计异常：", e);
            return ApiResponse.error("获取场馆收入统计失败");
        }
    }
    
    @Override
    public ApiResponse<Void> updateVenueStatus(Long id, Venue.VenueStatus status) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            venue.setStatus(status);
            venue.setUpdateTime(LocalDateTime.now());
            venueRepository.save(venue);
            
            log.info("更新场馆状态成功，场馆ID：{}，状态：{}", id, status);
            
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新场馆状态异常：", e);
            return ApiResponse.error("更新场馆状态失败");
        }
    }
    
    @Override
    public ApiResponse<Void> updateVenueOccupancy(Long id, Integer occupancy) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            venue.setCurrentOccupancy(occupancy);
            venue.setUpdateTime(LocalDateTime.now());
            venueRepository.save(venue);
            
            log.info("更新场馆使用人数成功，场馆ID：{}，人数：{}", id, occupancy);
            
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新场馆使用人数异常：", e);
            return ApiResponse.error("更新场馆使用人数失败");
        }
    }
    
    @Override
    public ApiResponse<Void> updateVenueRating(Long id, BigDecimal rating) {
        try {
            Venue venue = venueRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("场馆不存在"));
            
            // 更新评分
            BigDecimal currentRating = venue.getRating();
            Integer currentCount = venue.getRatingCount();
            
            BigDecimal newRating = currentRating.multiply(BigDecimal.valueOf(currentCount))
                    .add(rating)
                    .divide(BigDecimal.valueOf(currentCount + 1), 2, RoundingMode.HALF_UP);
            
            venue.setRating(newRating);
            venue.setRatingCount(currentCount + 1);
            venue.setUpdateTime(LocalDateTime.now());
            venueRepository.save(venue);
            
            log.info("更新场馆评分成功，场馆ID：{}，新评分：{}", id, newRating);
            
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("更新场馆评分异常：", e);
            return ApiResponse.error("更新场馆评分失败");
        }
    }
    
    @Override
    public ApiResponse<Void> batchUpdateVenueStatus(List<Long> ids, Venue.VenueStatus status) {
        try {
            for (Long id : ids) {
                updateVenueStatus(id, status);
            }
            
            log.info("批量更新场馆状态成功，场馆数量：{}，状态：{}", ids.size(), status);
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("批量更新场馆状态异常：", e);
            return ApiResponse.error("批量更新场馆状态失败");
        }
    }
    
    @Override
    public ApiResponse<Map<String, Object>> getVenueStatistics(Long merchantId) {
        try {
            Map<String, Object> statistics = new HashMap<>();
            
            // 总场馆数
            long totalVenues = venueRepository.countByMerchantId(merchantId);
            statistics.put("totalVenues", totalVenues);
            
            // 活跃场馆数
            long activeVenues = venueRepository.countByMerchantIdAndStatus(merchantId, Venue.VenueStatus.ACTIVE);
            statistics.put("activeVenues", activeVenues);
            
            // 停用场馆数
            long inactiveVenues = venueRepository.countByMerchantIdAndStatus(merchantId, Venue.VenueStatus.INACTIVE);
            statistics.put("inactiveVenues", inactiveVenues);
            
            // 维护中场馆数
            long maintenanceVenues = venueRepository.countByMerchantIdAndStatus(merchantId, Venue.VenueStatus.MAINTENANCE);
            statistics.put("maintenanceVenues", maintenanceVenues);
            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取场馆统计异常：", e);
            return ApiResponse.error("获取场馆统计失败");
        }
    }
    
    @Override
    public ApiResponse<Map<String, Long>> getVenueTypeStatistics(Long merchantId) {
        try {
            List<Object[]> typeStats = venueRepository.countByTypeAndMerchantId(merchantId);
            Map<String, Long> statistics = new HashMap<>();
            
            for (Object[] stat : typeStats) {
                String type = stat[0].toString();
                Long count = (Long) stat[1];
                statistics.put(type, count);
            }
            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取场馆类型统计异常：", e);
            return ApiResponse.error("获取场馆类型统计失败");
        }
    }
    
    @Override
    public ApiResponse<Boolean> checkVenueNameExists(String name, Long merchantId) {
        try {
            boolean exists = venueRepository.findByNameAndMerchantId(name, merchantId).isPresent();
            return ApiResponse.success(exists);
        } catch (Exception e) {
            log.error("检查场馆名称异常：", e);
            return ApiResponse.error("检查场馆名称失败");
        }
    }
    
    @Override
    public ApiResponse<List<Map<String, String>>> getVenueTypes() {
        try {
            List<Map<String, String>> types = Arrays.stream(Venue.VenueType.values())
                    .map(type -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("code", type.name());
                        map.put("description", type.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ApiResponse.success(types);
        } catch (Exception e) {
            log.error("获取场馆类型枚举异常：", e);
            return ApiResponse.error("获取场馆类型枚举失败");
        }
    }
    
    @Override
    public ApiResponse<List<Map<String, String>>> getVenueStatuses() {
        try {
            List<Map<String, String>> statuses = new ArrayList<>();
            for (Venue.VenueStatus status : Venue.VenueStatus.values()) {
                Map<String, String> statusMap = new HashMap<>();
                statusMap.put("code", status.name());
                statusMap.put("description", status.getDescription());
                statuses.add(statusMap);
            }
            return ApiResponse.success(statuses);
        } catch (Exception e) {
            log.error("获取场馆状态列表异常：", e);
            return ApiResponse.error("获取场馆状态列表失败");
        }
    }

    @Override
    public ApiResponse<List<Map<String, String>>> getVenueChargeTypes() {
        try {
            List<Map<String, String>> chargeTypes = new ArrayList<>();
            for (Venue.VenueChargeType chargeType : Venue.VenueChargeType.values()) {
                Map<String, String> chargeTypeMap = new HashMap<>();
                chargeTypeMap.put("code", chargeType.name());
                chargeTypeMap.put("description", chargeType.getDescription());
                chargeTypes.add(chargeTypeMap);
            }
            return ApiResponse.success(chargeTypes);
        } catch (Exception e) {
            log.error("获取场馆收费类型列表异常：", e);
            return ApiResponse.error("获取场馆收费类型列表失败");
        }
    }

    @Override
    public ApiResponse<List<Map<String, String>>> getVenueSpaceTypes() {
        try {
            List<Map<String, String>> spaceTypes = new ArrayList<>();
            for (Venue.VenueSpaceType spaceType : Venue.VenueSpaceType.values()) {
                Map<String, String> spaceTypeMap = new HashMap<>();
                spaceTypeMap.put("code", spaceType.name());
                spaceTypeMap.put("description", spaceType.getDescription());
                spaceTypes.add(spaceTypeMap);
            }
            return ApiResponse.success(spaceTypes);
        } catch (Exception e) {
            log.error("获取场馆空间类型列表异常：", e);
            return ApiResponse.error("获取场馆空间类型列表失败");
        }
    }
    
    @Override
    public ApiResponse<Void> importVenues(List<Venue> venues) {
        try {
            // TODO: 实现场馆数据导入
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("导入场馆数据异常：", e);
            return ApiResponse.error("导入场馆数据失败");
        }
    }
    
    @Override
    public ApiResponse<List<Venue>> exportVenues(VenueQueryDTO queryDTO) {
        try {
            // TODO: 实现场馆数据导出
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("导出场馆数据异常：", e);
            return ApiResponse.error("导出场馆数据失败");
        }
    }
    
    @Override
    public ApiResponse<Void> syncVenueData(Long venueId) {
        try {
            // TODO: 实现场馆数据同步
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("同步场馆数据异常：", e);
            return ApiResponse.error("同步场馆数据失败");
        }
    }
    
    @Override
    public ApiResponse<Void> backupVenueData(Long venueId) {
        try {
            // TODO: 实现场馆数据备份
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("备份场馆数据异常：", e);
            return ApiResponse.error("备份场馆数据失败");
        }
    }
    
    @Override
    public ApiResponse<Void> restoreVenueData(Long venueId) {
        try {
            // TODO: 实现场馆数据恢复
            return ApiResponse.error("功能未实现");
        } catch (Exception e) {
            log.error("恢复场馆数据异常：", e);
            return ApiResponse.error("恢复场馆数据失败");
        }
    }
    
    /**
     * 验证场馆信息
     */
    private void validateVenue(Venue venue) {
        if (!StringUtils.hasText(venue.getName())) {
            throw new BusinessException("场馆名称不能为空");
        }
        if (venue.getType() == null) {
            throw new BusinessException("场馆类型不能为空");
        }
        if (venue.getSpaceType() == null) {
            throw new BusinessException("场馆空间类型不能为空");
        }
        if (venue.getChargeType() == null) {
            throw new BusinessException("场馆收费类型不能为空");
        }
        if (venue.getMerchantId() == null) {
            throw new BusinessException("商户ID不能为空");
        }
        if (!StringUtils.hasText(venue.getAddress())) {
            throw new BusinessException("场馆地址不能为空");
        }
    }
    
    /**
     * 从List创建Page对象
     */
    private Page<Venue> createPageFromList(List<Venue> venues, Pageable pageable) {
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), venues.size());
        
        if (start > venues.size()) {
            return Page.empty(pageable);
        }
        
        return new org.springframework.data.domain.PageImpl<>(
                venues.subList(start, end), pageable, venues.size());
    }

    /**
     * 检查场馆是否营业
     */
    private boolean isVenueOpen(Venue venue) {
        if (venue.getStatus() != Venue.VenueStatus.ACTIVE) {
            return false;
        }
        
        String openTime = venue.getOpenTime();
        String closeTime = venue.getCloseTime();
        
        if (openTime == null || closeTime == null || openTime.isEmpty() || closeTime.isEmpty()) {
            return true; // 如果没有设置营业时间，默认营业
        }
        
        // 简化判断，实际应该解析时间字符串并考虑跨天营业的情况
        // 这里假设营业时间是24小时制，如"09:00"到"22:00"
        LocalDateTime now = LocalDateTime.now();
        String currentTime = String.format("%02d:%02d", now.getHour(), now.getMinute());
        
        return currentTime.compareTo(openTime) >= 0 && currentTime.compareTo(closeTime) <= 0;
    }
} 