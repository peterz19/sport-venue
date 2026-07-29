package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.merchant.*;
import com.sportvenue.venue.entity.Merchant;
import com.sportvenue.venue.entity.MerchantFeatures;
import com.sportvenue.venue.entity.PlatformAuditLog;
import com.sportvenue.venue.service.MerchantService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "平台商户管理")
@Slf4j
@RestController
@RequestMapping("/merchants")
@CrossOrigin(origins = "*")
public class MerchantController {

    @Autowired
    private MerchantService merchantService;

    @GetMapping
    public ApiResponse<List<Merchant>> getMerchants() {
        return merchantService.getMerchants();
    }

    @GetMapping("/{id}")
    public ApiResponse<Merchant> getMerchantById(@PathVariable("id") Long id) {
        return merchantService.getMerchantById(id);
    }

    @GetMapping("/{id}/overview")
    public ApiResponse<MerchantOverviewDTO> overview(@PathVariable("id") Long id) {
        return merchantService.overview(id);
    }

    @GetMapping("/{id}/audits")
    public ApiResponse<List<PlatformAuditLog>> audits(@PathVariable("id") Long id) {
        return merchantService.audits(id);
    }

    @PostMapping("/onboard")
    public ApiResponse<MerchantOnboardResult> onboard(@RequestBody MerchantOnboardRequest request) {
        return merchantService.onboard(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<Merchant> update(@PathVariable("id") Long id, @RequestBody MerchantUpdateRequest request) {
        return merchantService.update(id, request);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return merchantService.updateStatus(id, body.get("status"));
    }

    @GetMapping("/{id}/features")
    public ApiResponse<MerchantFeatures> getFeatures(@PathVariable("id") Long id) {
        return merchantService.getFeatures(id);
    }

    @PutMapping("/{id}/features")
    public ApiResponse<MerchantFeatures> updateFeatures(@PathVariable("id") Long id,
                                                        @RequestBody MerchantFeatures body) {
        return merchantService.updateFeatures(id, body);
    }

    @GetMapping("/{id}/wx-channels")
    public ApiResponse<List<Map<String, Object>>> listWxChannels(@PathVariable("id") Long id) {
        return merchantService.listWxChannels(id);
    }

    @PutMapping("/{id}/wx-channels")
    public ApiResponse<Map<String, Object>> upsertWxChannel(@PathVariable("id") Long id,
                                                            @RequestBody WxChannelUpsertRequest request) {
        return merchantService.upsertWxChannel(id, request);
    }

    @GetMapping("/{id}/wx-pay")
    public ApiResponse<Map<String, Object>> getWxPay(@PathVariable("id") Long id) {
        return merchantService.getWxPay(id);
    }

    @PutMapping("/{id}/wx-pay")
    public ApiResponse<Map<String, Object>> upsertWxPay(@PathVariable("id") Long id,
                                                        @RequestBody Map<String, String> body) {
        return merchantService.upsertWxPay(id, body);
    }
}
