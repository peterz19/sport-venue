package com.sportvenue.venue.service;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.entity.MerchantFeatures;
import com.sportvenue.venue.repository.MerchantFeaturesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MerchantFeatureService {

    public enum Feature {
        CASHIER, BOOKING, TEAM_MATCH, C_END, RECHARGE
    }

    @Autowired
    private MerchantFeaturesRepository featuresRepository;

    @Transactional
    public MerchantFeatures getOrCreate(Long merchantId) {
        return featuresRepository.findById(merchantId)
                .orElseGet(() -> featuresRepository.save(MerchantFeatures.defaults(merchantId)));
    }

    public void requireEnabled(Long merchantId, Feature feature) {
        MerchantFeatures f = getOrCreate(merchantId);
        boolean ok = switch (feature) {
            case CASHIER -> Boolean.TRUE.equals(f.getEnableCashier());
            case BOOKING -> Boolean.TRUE.equals(f.getEnableBooking());
            case TEAM_MATCH -> Boolean.TRUE.equals(f.getEnableTeamMatch());
            case C_END -> Boolean.TRUE.equals(f.getEnableCEnd());
            case RECHARGE -> Boolean.TRUE.equals(f.getEnableRecharge());
        };
        if (!ok) {
            throw new BusinessException(40303, "功能未开通：" + feature.name());
        }
    }

    @Transactional
    public MerchantFeatures update(Long merchantId, MerchantFeatures patch) {
        MerchantFeatures f = getOrCreate(merchantId);
        if (patch.getEnableCashier() != null) f.setEnableCashier(patch.getEnableCashier());
        if (patch.getEnableBooking() != null) f.setEnableBooking(patch.getEnableBooking());
        if (patch.getEnableTeamMatch() != null) f.setEnableTeamMatch(patch.getEnableTeamMatch());
        if (patch.getEnableCEnd() != null) f.setEnableCEnd(patch.getEnableCEnd());
        if (patch.getEnableRecharge() != null) f.setEnableRecharge(patch.getEnableRecharge());
        if (patch.getMaxStaff() != null) f.setMaxStaff(patch.getMaxStaff());
        if (patch.getMaxVenues() != null) f.setMaxVenues(patch.getMaxVenues());
        if (patch.getMaxCourts() != null) f.setMaxCourts(patch.getMaxCourts());
        if (patch.getMaxWxMini() != null) f.setMaxWxMini(patch.getMaxWxMini());
        f.setUpdateTime(LocalDateTime.now());
        return featuresRepository.save(f);
    }
}
