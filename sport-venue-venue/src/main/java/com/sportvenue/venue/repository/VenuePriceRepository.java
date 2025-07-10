package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.VenuePrice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 场馆价格策略数据访问层
 */
@Repository
public interface VenuePriceRepository extends JpaRepository<VenuePrice, Long> {
    
    /**
     * 根据场馆ID查询价格策略
     */
    List<VenuePrice> findByVenueId(Long venueId);
    
    /**
     * 根据场馆ID和启用状态查询
     */
    List<VenuePrice> findByVenueIdAndEnabled(Long venueId, Boolean enabled);
    
    /**
     * 根据场馆ID和价格类型查询
     */
    List<VenuePrice> findByVenueIdAndType(Long venueId, VenuePrice.PriceType type);
    
    /**
     * 根据场馆ID、类型和启用状态查询
     */
    List<VenuePrice> findByVenueIdAndTypeAndEnabled(Long venueId, VenuePrice.PriceType type, Boolean enabled);
    
    /**
     * 根据优先级排序查询场馆价格策略
     */
    List<VenuePrice> findByVenueIdAndEnabledOrderByPriorityDesc(Long venueId, Boolean enabled);
    
    /**
     * 查询在指定日期范围内有效的价格策略
     */
    @Query("SELECT vp FROM VenuePrice vp WHERE vp.venueId = :venueId AND vp.enabled = true " +
           "AND (vp.validFrom IS NULL OR vp.validFrom <= :date) " +
           "AND (vp.validTo IS NULL OR vp.validTo >= :date) " +
           "ORDER BY vp.priority DESC")
    List<VenuePrice> findValidPricesByDate(@Param("venueId") Long venueId, @Param("date") LocalDateTime date);
    
    /**
     * 查询在指定时段有效的价格策略
     */
    @Query("SELECT vp FROM VenuePrice vp WHERE vp.venueId = :venueId AND vp.enabled = true " +
           "AND (vp.timeSlotStart IS NULL OR vp.timeSlotStart <= :time) " +
           "AND (vp.timeSlotEnd IS NULL OR vp.timeSlotEnd >= :time) " +
           "ORDER BY vp.priority DESC")
    List<VenuePrice> findValidPricesByTime(@Param("venueId") Long venueId, @Param("time") String time);
    
    /**
     * 查询在指定星期有效的价格策略
     */
    @Query("SELECT vp FROM VenuePrice vp WHERE vp.venueId = :venueId AND vp.enabled = true " +
           "AND (vp.weekdays IS NULL OR vp.weekdays LIKE %:weekday%) " +
           "ORDER BY vp.priority DESC")
    List<VenuePrice> findValidPricesByWeekday(@Param("venueId") Long venueId, @Param("weekday") String weekday);
    
    /**
     * 复合查询：场馆ID + 日期 + 时间 + 星期
     */
    @Query("SELECT vp FROM VenuePrice vp WHERE vp.venueId = :venueId AND vp.enabled = true " +
           "AND (vp.validFrom IS NULL OR vp.validFrom <= :date) " +
           "AND (vp.validTo IS NULL OR vp.validTo >= :date) " +
           "AND (vp.timeSlotStart IS NULL OR vp.timeSlotStart <= :time) " +
           "AND (vp.timeSlotEnd IS NULL OR vp.timeSlotEnd >= :time) " +
           "AND (vp.weekdays IS NULL OR vp.weekdays LIKE %:weekday%) " +
           "ORDER BY vp.priority DESC")
    List<VenuePrice> findValidPrices(@Param("venueId") Long venueId, 
                                    @Param("date") LocalDateTime date,
                                    @Param("time") String time, 
                                    @Param("weekday") String weekday);
    
    /**
     * 删除场馆的所有价格策略
     */
    void deleteByVenueId(Long venueId);
    
    /**
     * 统计场馆的价格策略数量
     */
    long countByVenueId(Long venueId);
    
    /**
     * 统计启用的价格策略数量
     */
    long countByVenueIdAndEnabled(Long venueId, Boolean enabled);
} 