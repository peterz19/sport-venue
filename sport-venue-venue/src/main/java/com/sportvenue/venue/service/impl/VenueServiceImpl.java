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
            existingVenue.setSubType(venue.getSubType());
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
            Sort sort = Sort.by(Sort.Direction.fromString(queryDTO.getSortDirection()), queryDTO.getSortBy());
            Pageable pageable = PageRequest.of(queryDTO.getPage(), queryDTO.getSize(), sort);
            
            // 根据查询条件获取场馆列表
            Page<Venue> venuePage;
            
            if (queryDTO.getMerchantId() != null) {
                venuePage = venueRepository.findByMerchantId(queryDTO.getMerchantId(), pageable);
            } else if (queryDTO.getType() != null) {
                List<Venue> venues = venueRepository.findByType(queryDTO.getType());
                venuePage = createPageFromList(venues, pageable);
            } else if (queryDTO.getSubType() != null) {
                List<Venue> venues = venueRepository.findBySubType(queryDTO.getSubType());
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
    public ApiResponse<List<VenueDTO>> getVenuesBySubType(Venue.VenueSubType subType) {
        try {
            List<Venue> venues = venueRepository.findBySubType(subType);
            List<VenueDTO> dtoList = venues.stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("查询场馆子类型列表异常：", e);
            return ApiResponse.error("查询场馆子类型列表失败");
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
            Pageable pageable = PageRequest.of(0, limit != null ? limit : 10);
            Page<Venue> venuePage = venueRepository.findPopularVenues(pageable);
            List<VenueDTO> dtoList = venuePage.getContent().stream()
                    .map(VenueDTO::fromEntity)
                    .collect(Collectors.toList());
            
            return ApiResponse.success(dtoList);
        } catch (Exception e) {
            log.error("查询热门场馆异常：", e);
            return ApiResponse.error("查询热门场馆失败");
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
                    .divide(BigDecimal.valueOf(currentCount + 1), 2, BigDecimal.ROUND_HALF_UP);
            
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
    public ApiResponse<Map<String, Long>> getVenueSubTypeStatistics(Long merchantId) {
        try {
            List<Object[]> subTypeStats = venueRepository.countBySubTypeAndMerchantId(merchantId);
            Map<String, Long> statistics = new HashMap<>();
            
            for (Object[] stat : subTypeStats) {
                String subType = stat[0].toString();
                Long count = (Long) stat[1];
                statistics.put(subType, count);
            }
            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取场馆子类型统计异常：", e);
            return ApiResponse.error("获取场馆子类型统计失败");
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
    public ApiResponse<List<Map<String, String>>> getVenueSubTypes() {
        try {
            List<Map<String, String>> subTypes = Arrays.stream(Venue.VenueSubType.values())
                    .map(subType -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("code", subType.name());
                        map.put("description", subType.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ApiResponse.success(subTypes);
        } catch (Exception e) {
            log.error("获取场馆子类型枚举异常：", e);
            return ApiResponse.error("获取场馆子类型枚举失败");
        }
    }
    
    @Override
    public ApiResponse<List<Map<String, String>>> getVenueStatuses() {
        try {
            List<Map<String, String>> statuses = Arrays.stream(Venue.VenueStatus.values())
                    .map(status -> {
                        Map<String, String> map = new HashMap<>();
                        map.put("code", status.name());
                        map.put("description", status.getDescription());
                        return map;
                    })
                    .collect(Collectors.toList());
            
            return ApiResponse.success(statuses);
        } catch (Exception e) {
            log.error("获取场馆状态枚举异常：", e);
            return ApiResponse.error("获取场馆状态枚举失败");
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
        if (venue.getSubType() == null) {
            throw new BusinessException("场馆子类型不能为空");
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
} 