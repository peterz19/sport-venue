package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.VenueCheckIn;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 场馆打卡数据访问层
 */
@Repository
public interface VenueCheckInRepository extends JpaRepository<VenueCheckIn, Long> {
    
    /**
     * 根据打卡编号查询
     */
    Optional<VenueCheckIn> findByCheckInNo(String checkInNo);
    
    /**
     * 根据场馆ID查询打卡记录
     */
    List<VenueCheckIn> findByVenueId(Long venueId);
    
    /**
     * 根据场馆ID分页查询打卡记录
     */
    Page<VenueCheckIn> findByVenueId(Long venueId, Pageable pageable);
    
    /**
     * 根据用户ID查询打卡记录
     */
    List<VenueCheckIn> findByUserId(Long userId);
    
    /**
     * 根据用户ID分页查询打卡记录
     */
    Page<VenueCheckIn> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据打卡类型查询
     */
    List<VenueCheckIn> findByType(VenueCheckIn.CheckInType type);
    
    /**
     * 根据打卡方式查询
     */
    List<VenueCheckIn> findByMethod(VenueCheckIn.CheckInMethod method);
    
    /**
     * 根据场馆ID和打卡类型查询
     */
    List<VenueCheckIn> findByVenueIdAndType(Long venueId, VenueCheckIn.CheckInType type);
    
    /**
     * 根据用户ID和打卡类型查询
     */
    List<VenueCheckIn> findByUserIdAndType(Long userId, VenueCheckIn.CheckInType type);
    
    /**
     * 根据预约ID查询打卡记录
     */
    List<VenueCheckIn> findByReservationId(Long reservationId);
    
    /**
     * 查询用户今日打卡记录
     */
    @Query("SELECT vci FROM VenueCheckIn vci WHERE vci.userId = :userId " +
           "AND vci.type = 'CHECK_IN' " +
           "AND DATE(vci.checkInTime) = CURRENT_DATE " +
           "ORDER BY vci.checkInTime DESC")
    List<VenueCheckIn> findTodayCheckIns(@Param("userId") Long userId);
    
    /**
     * 查询场馆今日打卡记录
     */
    @Query("SELECT vci FROM VenueCheckIn vci WHERE vci.venueId = :venueId " +
           "AND DATE(vci.checkInTime) = CURRENT_DATE " +
           "ORDER BY vci.checkInTime DESC")
    List<VenueCheckIn> findTodayVenueCheckIns(@Param("venueId") Long venueId);
    
    /**
     * 查询用户本周打卡记录
     */
    @Query("SELECT vci FROM VenueCheckIn vci WHERE vci.userId = :userId " +
           "AND vci.type = 'CHECK_IN' " +
           "AND vci.checkInTime >= :weekStart AND vci.checkInTime < :weekEnd " +
           "ORDER BY vci.checkInTime DESC")
    List<VenueCheckIn> findThisWeekCheckIns(@Param("userId") Long userId,
                                           @Param("weekStart") LocalDateTime weekStart,
                                           @Param("weekEnd") LocalDateTime weekEnd);
    
    /**
     * 查询用户本月打卡记录
     */
    @Query("SELECT vci FROM VenueCheckIn vci WHERE vci.userId = :userId " +
           "AND vci.type = 'CHECK_IN' " +
           "AND vci.checkInTime >= :monthStart AND vci.checkInTime < :monthEnd " +
           "ORDER BY vci.checkInTime DESC")
    List<VenueCheckIn> findThisMonthCheckIns(@Param("userId") Long userId,
                                            @Param("monthStart") LocalDateTime monthStart,
                                            @Param("monthEnd") LocalDateTime monthEnd);
    
    /**
     * 查询用户连续打卡天数
     */
    @Query("SELECT COUNT(DISTINCT DATE(vci.checkInTime)) FROM VenueCheckIn vci " +
           "WHERE vci.userId = :userId AND vci.type = 'CHECK_IN' " +
           "AND vci.checkInTime >= :startDate")
    long countConsecutiveDays(@Param("userId") Long userId, @Param("startDate") LocalDateTime startDate);
    
    /**
     * 查询用户总打卡次数
     */
    @Query("SELECT COUNT(vci) FROM VenueCheckIn vci WHERE vci.userId = :userId AND vci.type = 'CHECK_IN'")
    long countTotalCheckIns(@Param("userId") Long userId);
    
    /**
     * 查询用户在指定场馆的打卡次数
     */
    @Query("SELECT COUNT(vci) FROM VenueCheckIn vci WHERE vci.userId = :userId " +
           "AND vci.venueId = :venueId AND vci.type = 'CHECK_IN'")
    long countUserVenueCheckIns(@Param("userId") Long userId, @Param("venueId") Long venueId);
    
    /**
     * 查询用户获得的积分总数
     */
    @Query("SELECT COALESCE(SUM(vci.earnedPoints), 0) FROM VenueCheckIn vci WHERE vci.userId = :userId")
    Integer sumUserEarnedPoints(@Param("userId") Long userId);
    
    /**
     * 查询场馆今日获得的积分总数
     */
    @Query("SELECT COALESCE(SUM(vci.earnedPoints), 0) FROM VenueCheckIn vci " +
           "WHERE vci.venueId = :venueId AND DATE(vci.checkInTime) = CURRENT_DATE")
    Integer sumTodayVenuePoints(@Param("venueId") Long venueId);
    
    /**
     * 查询用户最近一次打卡记录
     */
    @Query("SELECT vci FROM VenueCheckIn vci WHERE vci.userId = :userId " +
           "AND vci.type = 'CHECK_IN' " +
           "ORDER BY vci.checkInTime DESC")
    Page<VenueCheckIn> findLatestCheckIn(@Param("userId") Long userId, Pageable pageable);
    
    /**
     * 查询场馆当前在线用户数
     */
    @Query("SELECT COUNT(DISTINCT vci.userId) FROM VenueCheckIn vci " +
           "WHERE vci.venueId = :venueId " +
           "AND vci.type = 'CHECK_IN' " +
           "AND vci.checkInTime >= :startTime " +
           "AND NOT EXISTS (SELECT 1 FROM VenueCheckIn vci2 WHERE vci2.userId = vci.userId " +
           "AND vci2.venueId = :venueId AND vci2.type = 'CHECK_OUT' " +
           "AND vci2.checkInTime > vci.checkInTime)")
    long countCurrentOnlineUsers(@Param("venueId") Long venueId, @Param("startTime") LocalDateTime startTime);
    
    /**
     * 查询用户是否在指定场馆在线
     */
    @Query("SELECT CASE WHEN COUNT(vci) > 0 THEN true ELSE false END FROM VenueCheckIn vci " +
           "WHERE vci.userId = :userId AND vci.venueId = :venueId " +
           "AND vci.type = 'CHECK_IN' " +
           "AND vci.checkInTime >= :startTime " +
           "AND NOT EXISTS (SELECT 1 FROM VenueCheckIn vci2 WHERE vci2.userId = :userId " +
           "AND vci2.venueId = :venueId AND vci2.type = 'CHECK_OUT' " +
           "AND vci2.checkInTime > vci.checkInTime)")
    boolean isUserOnline(@Param("userId") Long userId, 
                        @Param("venueId") Long venueId, 
                        @Param("startTime") LocalDateTime startTime);
    
    /**
     * 统计场馆打卡记录数量
     */
    long countByVenueId(Long venueId);
    
    /**
     * 统计用户打卡记录数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计今日打卡记录数量
     */
    @Query("SELECT COUNT(vci) FROM VenueCheckIn vci WHERE DATE(vci.checkInTime) = CURRENT_DATE")
    long countTodayCheckIns();
    
    /**
     * 统计场馆今日打卡记录数量
     */
    @Query("SELECT COUNT(vci) FROM VenueCheckIn vci WHERE vci.venueId = :venueId AND DATE(vci.checkInTime) = CURRENT_DATE")
    long countTodayVenueCheckIns(@Param("venueId") Long venueId);
} 