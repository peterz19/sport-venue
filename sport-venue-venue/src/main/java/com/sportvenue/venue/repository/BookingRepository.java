package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Booking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByIdAndMerchantId(Long id, Long merchantId);

    @Query("SELECT b FROM Booking b WHERE b.courtId = :courtId " +
           "AND b.status IN :statuses " +
           "AND b.startTime < :end AND b.endTime > :start " +
           "AND (:excludeId IS NULL OR b.id <> :excludeId)")
    List<Booking> findConflicts(@Param("courtId") Long courtId,
                                @Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end,
                                @Param("statuses") List<Booking.BookingStatus> statuses,
                                @Param("excludeId") Long excludeId);

    @Query("SELECT b FROM Booking b WHERE b.merchantId = :merchantId " +
           "AND (:venueId IS NULL OR b.venueId = :venueId) " +
           "AND (:courtId IS NULL OR b.courtId = :courtId) " +
           "AND (:status IS NULL OR b.status = :status) " +
           "AND (:bookType IS NULL OR b.bookType = :bookType) " +
           "AND (:operatorId IS NULL OR b.operatorId = :operatorId) " +
           "AND (:start IS NULL OR b.startTime >= :start) " +
           "AND (:end IS NULL OR b.startTime < :end) " +
           "ORDER BY b.startTime DESC")
    Page<Booking> search(@Param("merchantId") Long merchantId,
                         @Param("venueId") Long venueId,
                         @Param("courtId") Long courtId,
                         @Param("status") Booking.BookingStatus status,
                         @Param("bookType") Booking.BookType bookType,
                         @Param("operatorId") Long operatorId,
                         @Param("start") LocalDateTime start,
                         @Param("end") LocalDateTime end,
                         Pageable pageable);

    @Query("SELECT b FROM Booking b WHERE b.courtId = :courtId " +
           "AND b.status IN :statuses " +
           "AND b.startTime < :dayEnd AND b.endTime > :dayStart " +
           "ORDER BY b.startTime ASC")
    List<Booking> findDayOccupancy(@Param("courtId") Long courtId,
                                   @Param("dayStart") LocalDateTime dayStart,
                                   @Param("dayEnd") LocalDateTime dayEnd,
                                   @Param("statuses") List<Booking.BookingStatus> statuses);

    List<Booking> findTop10ByTeamIdAndMerchantIdOrderByStartTimeDesc(Long teamId, Long merchantId);

    @Query("SELECT b.operatorId, COUNT(b), COALESCE(SUM(b.amount), 0) FROM Booking b " +
           "WHERE b.merchantId = :merchantId AND b.status IN :statuses " +
           "AND b.createTime >= :start AND b.createTime < :end " +
           "AND b.operatorId IS NOT NULL GROUP BY b.operatorId")
    List<Object[]> aggregateByOperator(@Param("merchantId") Long merchantId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("statuses") List<Booking.BookingStatus> statuses);

    @Query("SELECT b.liaisonStaffId, COUNT(b), COALESCE(SUM(b.amount), 0) FROM Booking b " +
           "WHERE b.merchantId = :merchantId AND b.status IN :statuses " +
           "AND b.bookType = :teamType " +
           "AND b.createTime >= :start AND b.createTime < :end " +
           "AND b.liaisonStaffId IS NOT NULL GROUP BY b.liaisonStaffId")
    List<Object[]> aggregateByLiaison(@Param("merchantId") Long merchantId,
                                      @Param("start") LocalDateTime start,
                                      @Param("end") LocalDateTime end,
                                      @Param("statuses") List<Booking.BookingStatus> statuses,
                                      @Param("teamType") Booking.BookType teamType);

    @Query("SELECT COUNT(b), COALESCE(SUM(b.amount), 0) FROM Booking b " +
           "WHERE b.merchantId = :merchantId AND b.operatorId = :staffId " +
           "AND b.status IN :statuses AND b.createTime >= :start AND b.createTime < :end")
    List<Object[]> aggregateOperateOne(@Param("merchantId") Long merchantId,
                                       @Param("staffId") Long staffId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("statuses") List<Booking.BookingStatus> statuses);

    @Query("SELECT COUNT(b), COALESCE(SUM(b.amount), 0) FROM Booking b " +
           "WHERE b.merchantId = :merchantId AND b.liaisonStaffId = :staffId " +
           "AND b.bookType = :teamType " +
           "AND b.status IN :statuses AND b.createTime >= :start AND b.createTime < :end")
    List<Object[]> aggregateLiaisonOne(@Param("merchantId") Long merchantId,
                                       @Param("staffId") Long staffId,
                                       @Param("start") LocalDateTime start,
                                       @Param("end") LocalDateTime end,
                                       @Param("statuses") List<Booking.BookingStatus> statuses,
                                       @Param("teamType") Booking.BookType teamType);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(order_no, 11) AS UNSIGNED)), 0) " +
                   "FROM bookings WHERE order_no LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Long findMaxSeqByPrefix(@Param("prefix") String prefix);
}
