package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.CustomerWallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerWalletRepository extends JpaRepository<CustomerWallet, Long> {
    Optional<CustomerWallet> findByMerchantIdAndCustomerUserId(Long merchantId, Long customerUserId);
}
