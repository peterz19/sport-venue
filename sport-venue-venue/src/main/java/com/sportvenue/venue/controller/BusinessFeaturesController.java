package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.MerchantFeatures;
import com.sportvenue.venue.service.MerchantFeatureService;
import com.sportvenue.venue.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/business/features")
public class BusinessFeaturesController {

    @Autowired
    private MerchantFeatureService featureService;

    @GetMapping
    public ApiResponse<MerchantFeatures> mine() {
        Long merchantId = SecurityUtils.requireMerchantId();
        return ApiResponse.success(featureService.getOrCreate(merchantId));
    }
}
