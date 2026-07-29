package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.merchant.*;
import com.sportvenue.venue.entity.Merchant;
import com.sportvenue.venue.entity.MerchantFeatures;
import com.sportvenue.venue.entity.MerchantWxPay;
import com.sportvenue.venue.entity.PlatformAuditLog;

import java.util.List;
import java.util.Map;

public interface MerchantService {

    ApiResponse<List<Merchant>> getMerchants();

    ApiResponse<Merchant> getMerchantById(Long id);

    ApiResponse<MerchantOnboardResult> onboard(MerchantOnboardRequest request);

    ApiResponse<Merchant> update(Long id, MerchantUpdateRequest request);

    ApiResponse<Void> updateStatus(Long id, String status);

    ApiResponse<MerchantOverviewDTO> overview(Long id);

    ApiResponse<List<PlatformAuditLog>> audits(Long id);

    ApiResponse<MerchantFeatures> getFeatures(Long id);

    ApiResponse<MerchantFeatures> updateFeatures(Long id, MerchantFeatures patch);

    ApiResponse<List<Map<String, Object>>> listWxChannels(Long id);

    ApiResponse<Map<String, Object>> upsertWxChannel(Long id, WxChannelUpsertRequest request);

    ApiResponse<Map<String, Object>> getWxPay(Long id);

    ApiResponse<Map<String, Object>> upsertWxPay(Long id, Map<String, String> body);
}
