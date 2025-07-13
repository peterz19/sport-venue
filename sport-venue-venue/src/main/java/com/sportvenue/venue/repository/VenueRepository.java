package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Venue;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * 场馆数据访问层
 * 支持多商户、多类型场馆查询
 */
@Repository
public interface VenueRepository extends JpaRepository<Venue, Long> {
    
    /**
     * 根据商户ID查询场馆列表
     */
    List<Venue> findByMerchantId(Long merchantId);
    
    /**
     * 根据商户ID分页查询场馆
     */
    Page<Venue> findByMerchantId(Long merchantId, Pageable pageable);
    
    /**
     * 根据场馆类型查询
     */
    List<Venue> findByType(Venue.VenueType type);
    
    /**
     * 根据场馆空间类型查询
     */
    List<Venue> findBySpaceType(Venue.VenueSpaceType spaceType);
    
    /**
     * 根据场馆收费类型查询
     */
    List<Venue> findByChargeType(Venue.VenueChargeType chargeType);
    
    /**
     * 根据场馆状态查询
     */
    List<Venue> findByStatus(Venue.VenueStatus status);
    
    /**
     * 根据商户ID和状态查询
     */
    List<Venue> findByMerchantIdAndStatus(Long merchantId, Venue.VenueStatus status);
    
    /**
     * 根据场馆名称模糊查询
     */
    List<Venue> findByNameContaining(String name);
    
    /**
     * 根据地址模糊查询
     */
    List<Venue> findByAddressContaining(String address);
    
    /**
     * 根据地理位置范围查询（经纬度范围）
     */
    @Query("SELECT v FROM Venue v WHERE v.longitude BETWEEN :minLng AND :maxLng " +
           "AND v.latitude BETWEEN :minLat AND :maxLat AND v.status = 'ACTIVE'")
    List<Venue> findByLocationRange(@Param("minLng") BigDecimal minLng, 
                                   @Param("maxLng") BigDecimal maxLng,
                                   @Param("minLat") BigDecimal minLat, 
                                   @Param("maxLat") BigDecimal maxLat);
    
    /**
     * 根据评分范围查询
     */
    @Query("SELECT v FROM Venue v WHERE v.rating >= :minRating AND v.status = 'ACTIVE'")
    List<Venue> findByRatingGreaterThanEqual(@Param("minRating") BigDecimal minRating);
    
    /**
     * 根据容量范围查询
     */
    @Query("SELECT v FROM Venue v WHERE v.capacity BETWEEN :minCapacity AND :maxCapacity AND v.status = 'ACTIVE'")
    List<Venue> findByCapacityRange(@Param("minCapacity") Integer minCapacity, 
                                   @Param("maxCapacity") Integer maxCapacity);
    
    /**
     * 查询支持预约的场馆
     */
    List<Venue> findByReservationEnabledTrueAndStatus(Venue.VenueStatus status);
    
    /**
     * 查询支持打卡的场馆
     */
    List<Venue> findByCheckInEnabledTrueAndStatus(Venue.VenueStatus status);
    
    /**
     * 查询支持积分的场馆
     */
    List<Venue> findByPointsEnabledTrueAndStatus(Venue.VenueStatus status);
    
    /**
     * 根据商户ID和场馆类型查询
     */
    List<Venue> findByMerchantIdAndType(Long merchantId, Venue.VenueType type);
    
    /**
     * 复合查询：商户ID + 类型 + 状态
     */
    List<Venue> findByMerchantIdAndTypeAndStatus(Long merchantId, 
                                                Venue.VenueType type, 
                                                Venue.VenueStatus status);
    

    /**
     * 根据场馆名称和商户ID查询
     */
    Optional<Venue> findByNameAndMerchantId(String name, Long merchantId);
    
    /**
     * 统计商户的场馆数量
     */
    long countByMerchantId(Long merchantId);
    
    /**
     * 统计商户的活跃场馆数量
     */
    long countByMerchantIdAndStatus(Long merchantId, Venue.VenueStatus status);
    
    /**
     * 统计各类型场馆数量
     */
    @Query("SELECT v.type, COUNT(v) FROM Venue v WHERE v.merchantId = :merchantId GROUP BY v.type")
    List<Object[]> countByTypeAndMerchantId(@Param("merchantId") Long merchantId);
    
    /**
     * 查询热门场馆（按评分排序）
     */
    @Query("SELECT v FROM Venue v WHERE v.status = 'ACTIVE' ORDER BY v.rating DESC, v.ratingCount DESC")
    Page<Venue> findPopularVenues(Pageable pageable);
    
    /**
     * 查询附近场馆（按距离排序）
     */
    @Query("SELECT v FROM Venue v WHERE v.status = 'ACTIVE' " +
           "AND v.longitude BETWEEN :minLng AND :maxLng " +
           "AND v.latitude BETWEEN :minLat AND :maxLat")
    Page<Venue> findNearbyVenues(@Param("minLng") BigDecimal minLng,
                                 @Param("maxLng") BigDecimal maxLng,
                                 @Param("minLat") BigDecimal minLat,
                                 @Param("maxLat") BigDecimal maxLat,
                                 Pageable pageable);

    /**
     * 根据状态和评分排序查询场馆
     */
    List<Venue> findByStatusOrderByRatingDescRatingCountDesc(Venue.VenueStatus status);

    /**
     * 根据名称包含和状态查询场馆
     */
    List<Venue> findByNameContainingAndStatus(String name, Venue.VenueStatus status);

    /**
     * 根据状态和距离排序查询场馆（简化版本）
     */
    @Query("SELECT v FROM Venue v WHERE v.status = :status ORDER BY v.rating DESC")
    List<Venue> findByStatusOrderByRatingDesc(@Param("status") Venue.VenueStatus status);

    /**
     * 根据状态和距离排序查询场馆（带位置参数）
     */
    @Query("SELECT v FROM Venue v WHERE v.status = :status " +
           "ORDER BY SQRT(POWER(v.longitude - :longitude, 2) + POWER(v.latitude - :latitude, 2)) ASC")
    List<Venue> findByStatusOrderByDistanceAsc(@Param("status") Venue.VenueStatus status,
                                              @Param("longitude") BigDecimal longitude,
                                              @Param("latitude") BigDecimal latitude);
} 