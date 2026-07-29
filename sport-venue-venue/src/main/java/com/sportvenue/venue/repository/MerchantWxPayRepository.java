package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.MerchantWxPay;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MerchantWxPayRepository extends JpaRepository<MerchantWxPay, Long> {
    Optional<MerchantWxPay> findByMchId(String mchId);
}
