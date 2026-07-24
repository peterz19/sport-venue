package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {

    Optional<SalesOrder> findByIdAndMerchantId(Long id, Long merchantId);

    Optional<SalesOrder> findByOrderNoAndMerchantId(String orderNo, Long merchantId);

    @Query("SELECT o FROM SalesOrder o WHERE o.merchantId = :merchantId " +
           "AND (:venueId IS NULL OR o.venueId = :venueId) " +
           "AND (:status IS NULL OR o.status = :status) " +
           "AND (:payMethod IS NULL OR o.payMethod = :payMethod) " +
           "AND (:operatorId IS NULL OR o.operatorId = :operatorId) " +
           "AND (:start IS NULL OR o.paidAt >= :start) " +
           "AND (:end IS NULL OR o.paidAt < :end) " +
           "ORDER BY o.createTime DESC")
    Page<SalesOrder> searchOrders(@Param("merchantId") Long merchantId,
                                  @Param("venueId") Long venueId,
                                  @Param("status") SalesOrder.OrderStatus status,
                                  @Param("payMethod") SalesOrder.PayMethod payMethod,
                                  @Param("operatorId") Long operatorId,
                                  @Param("start") LocalDateTime start,
                                  @Param("end") LocalDateTime end,
                                  Pageable pageable);

    @Query("SELECT o FROM SalesOrder o WHERE o.merchantId = :merchantId " +
           "AND o.status = :paidStatus " +
           "AND (:venueId IS NULL OR o.venueId = :venueId) " +
           "AND (:operatorId IS NULL OR o.operatorId = :operatorId) " +
           "AND o.paidAt >= :start AND o.paidAt < :end")
    List<SalesOrder> findPaidOrdersInRange(@Param("merchantId") Long merchantId,
                                           @Param("venueId") Long venueId,
                                           @Param("operatorId") Long operatorId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("paidStatus") SalesOrder.OrderStatus paidStatus);

    @Query("SELECT o.operatorId, COUNT(o), COALESCE(SUM(o.totalAmount), 0), COALESCE(SUM(o.totalQty), 0) " +
           "FROM SalesOrder o WHERE o.merchantId = :merchantId " +
           "AND o.status = :paidStatus " +
           "AND o.paidAt >= :start AND o.paidAt < :end " +
           "AND o.operatorId IS NOT NULL " +
           "GROUP BY o.operatorId")
    List<Object[]> aggregatePaidByOperator(@Param("merchantId") Long merchantId,
                                           @Param("start") LocalDateTime start,
                                           @Param("end") LocalDateTime end,
                                           @Param("paidStatus") SalesOrder.OrderStatus paidStatus);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(order_no, 11) AS UNSIGNED)), 0) " +
                   "FROM sales_orders WHERE order_no LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Long findMaxSeqByPrefix(@Param("prefix") String prefix);
}
