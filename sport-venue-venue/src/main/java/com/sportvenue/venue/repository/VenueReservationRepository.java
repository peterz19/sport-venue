package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.VenueReservation;
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
 * 场馆预约数据访问层
 */
@Repository
public interface VenueReservationRepository extends JpaRepository<VenueReservation, Long> {
    
    /**
     * 根据预约编号查询
     */
    Optional<VenueReservation> findByReservationNo(String reservationNo);
    
    /**
     * 根据场馆ID查询预约
     */
    List<VenueReservation> findByVenueId(Long venueId);
    
    /**
     * 根据场馆ID分页查询预约
     */
    Page<VenueReservation> findByVenueId(Long venueId, Pageable pageable);
    
    /**
     * 根据用户ID查询预约
     */
    List<VenueReservation> findByUserId(Long userId);
    
    /**
     * 根据用户ID分页查询预约
     */
    Page<VenueReservation> findByUserId(Long userId, Pageable pageable);
    
    /**
     * 根据预约状态查询
     */
    List<VenueReservation> findByStatus(VenueReservation.ReservationStatus status);
    
    /**
     * 根据支付状态查询
     */
    List<VenueReservation> findByPaymentStatus(VenueReservation.PaymentStatus paymentStatus);
    
    /**
     * 根据场馆ID和状态查询
     */
    List<VenueReservation> findByVenueIdAndStatus(Long venueId, VenueReservation.ReservationStatus status);
    
    /**
     * 根据用户ID和状态查询
     */
    List<VenueReservation> findByUserIdAndStatus(Long userId, VenueReservation.ReservationStatus status);
    
    /**
     * 根据预约类型查询
     */
    List<VenueReservation> findByType(VenueReservation.ReservationType type);
    
    /**
     * 根据场馆ID和预约类型查询
     */
    List<VenueReservation> findByVenueIdAndType(Long venueId, VenueReservation.ReservationType type);
    
    /**
     * 查询指定时间范围内的预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND ((vr.startTime BETWEEN :startTime AND :endTime) " +
           "OR (vr.endTime BETWEEN :startTime AND :endTime) " +
           "OR (vr.startTime <= :startTime AND vr.endTime >= :endTime)) " +
           "AND vr.status IN ('PENDING', 'CONFIRMED')")
    List<VenueReservation> findConflictingReservations(@Param("venueId") Long venueId,
                                                      @Param("startTime") LocalDateTime startTime,
                                                      @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询用户指定时间范围内的预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.userId = :userId " +
           "AND ((vr.startTime BETWEEN :startTime AND :endTime) " +
           "OR (vr.endTime BETWEEN :startTime AND :endTime) " +
           "OR (vr.startTime <= :startTime AND vr.endTime >= :endTime)) " +
           "AND vr.status IN ('PENDING', 'CONFIRMED')")
    List<VenueReservation> findUserConflictingReservations(@Param("userId") Long userId,
                                                          @Param("startTime") LocalDateTime startTime,
                                                          @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询今日预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND DATE(vr.startTime) = CURRENT_DATE " +
           "ORDER BY vr.startTime ASC")
    List<VenueReservation> findTodayReservations(@Param("venueId") Long venueId);
    
    /**
     * 查询本周预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND vr.startTime >= :weekStart AND vr.startTime < :weekEnd " +
           "ORDER BY vr.startTime ASC")
    List<VenueReservation> findThisWeekReservations(@Param("venueId") Long venueId,
                                                   @Param("weekStart") LocalDateTime weekStart,
                                                   @Param("weekEnd") LocalDateTime weekEnd);
    
    /**
     * 查询本月预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND vr.startTime >= :monthStart AND vr.startTime < :monthEnd " +
           "ORDER BY vr.startTime ASC")
    List<VenueReservation> findThisMonthReservations(@Param("venueId") Long venueId,
                                                    @Param("monthStart") LocalDateTime monthStart,
                                                    @Param("monthEnd") LocalDateTime monthEnd);
    
    /**
     * 查询即将到期的预约（30分钟内）
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND vr.status = 'CONFIRMED' " +
           "AND vr.startTime BETWEEN :now AND :endTime " +
           "ORDER BY vr.startTime ASC")
    List<VenueReservation> findUpcomingReservations(@Param("venueId") Long venueId,
                                                   @Param("now") LocalDateTime now,
                                                   @Param("endTime") LocalDateTime endTime);
    
    /**
     * 查询过期的预约
     */
    @Query("SELECT vr FROM VenueReservation vr WHERE vr.venueId = :venueId " +
           "AND vr.status = 'CONFIRMED' " +
           "AND vr.endTime < :now " +
           "ORDER BY vr.endTime DESC")
    List<VenueReservation> findExpiredReservations(@Param("venueId") Long venueId,
                                                  @Param("now") LocalDateTime now);
    
    /**
     * 统计场馆的预约数量
     */
    long countByVenueId(Long venueId);
    
    /**
     * 统计场馆各状态预约数量
     */
    @Query("SELECT vr.status, COUNT(vr) FROM VenueReservation vr WHERE vr.venueId = :venueId GROUP BY vr.status")
    List<Object[]> countByStatusAndVenueId(@Param("venueId") Long venueId);
    
    /**
     * 统计用户预约数量
     */
    long countByUserId(Long userId);
    
    /**
     * 统计用户各状态预约数量
     */
    @Query("SELECT vr.status, COUNT(vr) FROM VenueReservation vr WHERE vr.userId = :userId GROUP BY vr.status")
    List<Object[]> countByStatusAndUserId(@Param("userId") Long userId);
    
    /**
     * 统计今日预约数量
     */
    @Query("SELECT COUNT(vr) FROM VenueReservation vr WHERE vr.venueId = :venueId AND DATE(vr.startTime) = CURRENT_DATE")
    long countTodayReservations(@Param("venueId") Long venueId);
    
    /**
     * 统计今日收入
     */
    @Query("SELECT COALESCE(SUM(vr.actualAmount), 0) FROM VenueReservation vr " +
           "WHERE vr.venueId = :venueId AND vr.paymentStatus = 'PAID' AND DATE(vr.paymentTime) = CURRENT_DATE")
    Double sumTodayRevenue(@Param("venueId") Long venueId);
    
    /**
     * 统计本月收入
     */
    @Query("SELECT COALESCE(SUM(vr.actualAmount), 0) FROM VenueReservation vr " +
           "WHERE vr.venueId = :venueId AND vr.paymentStatus = 'PAID' " +
           "AND vr.paymentTime >= :monthStart AND vr.paymentTime < :monthEnd")
    Double sumThisMonthRevenue(@Param("venueId") Long venueId,
                              @Param("monthStart") LocalDateTime monthStart,
                              @Param("monthEnd") LocalDateTime monthEnd);
} 