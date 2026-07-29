package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.WalletLedger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WalletLedgerRepository extends JpaRepository<WalletLedger, Long> {
    List<WalletLedger> findTop50ByMerchantIdAndCustomerUserIdOrderByCreateTimeDesc(Long merchantId, Long customerUserId);
}
