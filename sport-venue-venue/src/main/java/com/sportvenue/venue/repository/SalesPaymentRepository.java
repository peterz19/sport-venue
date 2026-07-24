package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.SalesPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesPaymentRepository extends JpaRepository<SalesPayment, Long> {

    List<SalesPayment> findByOrderId(Long orderId);

    Optional<SalesPayment> findFirstByOrderIdAndStatus(Long orderId, SalesPayment.PaymentStatus status);

    @Query(value = "SELECT COALESCE(MAX(CAST(SUBSTRING(payment_no, 11) AS UNSIGNED)), 0) " +
                   "FROM sales_payments WHERE payment_no LIKE CONCAT(:prefix, '%')", nativeQuery = true)
    Long findMaxSeqByPrefix(@Param("prefix") String prefix);
}
