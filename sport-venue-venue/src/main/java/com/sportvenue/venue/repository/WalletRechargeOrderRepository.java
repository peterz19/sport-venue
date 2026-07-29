package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.WalletRechargeOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRechargeOrderRepository extends JpaRepository<WalletRechargeOrder, Long> {
    Optional<WalletRechargeOrder> findByOrderNo(String orderNo);
}
