package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.MerchantWxChannel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchantWxChannelRepository extends JpaRepository<MerchantWxChannel, Long> {
    List<MerchantWxChannel> findByMerchantId(Long merchantId);

    Optional<MerchantWxChannel> findByMerchantIdAndChannelType(Long merchantId, MerchantWxChannel.ChannelType channelType);

    Optional<MerchantWxChannel> findByAppId(String appId);

    boolean existsByAppIdAndMerchantIdNot(String appId, Long merchantId);
}
