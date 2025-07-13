package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.Merchant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 商户Repository
 */
@Repository
public interface MerchantRepository extends JpaRepository<Merchant, Long> {
} 