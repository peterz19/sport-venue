package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.PlatformCommissionSettlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformCommissionSettlementRepository extends JpaRepository<PlatformCommissionSettlement, Long> {
    List<PlatformCommissionSettlement> findByMerchantIdOrderBySettledAtDesc(Long merchantId);

    List<PlatformCommissionSettlement> findTop50ByOrderBySettledAtDesc();
}
