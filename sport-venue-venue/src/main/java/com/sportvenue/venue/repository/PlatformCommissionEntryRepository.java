package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.PlatformCommissionEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PlatformCommissionEntryRepository extends JpaRepository<PlatformCommissionEntry, Long> {

    Optional<PlatformCommissionEntry> findByBizTypeAndBizId(String bizType, Long bizId);

    List<PlatformCommissionEntry> findByMerchantIdAndStatusOrderByPaidAtDesc(
            Long merchantId, PlatformCommissionEntry.EntryStatus status);

    @Query("SELECT e FROM PlatformCommissionEntry e WHERE e.merchantId = :merchantId " +
           "AND e.status = :status AND e.paidAt >= :start AND e.paidAt < :end ORDER BY e.paidAt ASC")
    List<PlatformCommissionEntry> findPendingInPeriod(@Param("merchantId") Long merchantId,
                                                      @Param("status") PlatformCommissionEntry.EntryStatus status,
                                                      @Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    @Query("SELECT COALESCE(SUM(e.commissionAmount), 0) FROM PlatformCommissionEntry e " +
           "WHERE e.merchantId = :merchantId AND e.status = 'PENDING'")
    BigDecimal sumPendingByMerchant(@Param("merchantId") Long merchantId);

    @Query("SELECT e.merchantId, COALESCE(SUM(e.commissionAmount), 0), COUNT(e) FROM PlatformCommissionEntry e " +
           "WHERE e.status = 'PENDING' GROUP BY e.merchantId")
    List<Object[]> aggregatePendingByMerchant();

    List<PlatformCommissionEntry> findBySettlementIdOrderByPaidAtAsc(Long settlementId);
}
