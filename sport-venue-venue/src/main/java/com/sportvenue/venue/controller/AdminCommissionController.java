package com.sportvenue.venue.controller;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.MerchantCommissionRule;
import com.sportvenue.venue.entity.PlatformCommissionSettlement;
import com.sportvenue.venue.service.PlatformCommissionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Tag(name = "平台抽成台账")
@Slf4j
@RestController
@RequestMapping("/admin/commissions")
@CrossOrigin(origins = "*")
public class AdminCommissionController {

    @Autowired
    private PlatformCommissionService commissionService;

    /** 商户应收汇总 */
    @GetMapping("/summary")
    public ApiResponse<List<Map<String, Object>>> summary() {
        return ApiResponse.success(commissionService.merchantSummaries());
    }

    @GetMapping("/merchants/{merchantId}")
    public ApiResponse<Map<String, Object>> merchantDetail(@PathVariable("merchantId") Long merchantId) {
        try {
            return ApiResponse.success(commissionService.merchantDetail(merchantId));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/merchants/{merchantId}/rule")
    public ApiResponse<MerchantCommissionRule> getRule(@PathVariable("merchantId") Long merchantId) {
        return ApiResponse.success(commissionService.getRule(merchantId));
    }

    @PutMapping("/merchants/{merchantId}/rule")
    public ApiResponse<MerchantCommissionRule> updateRule(@PathVariable("merchantId") Long merchantId,
                                                          @RequestBody MerchantCommissionRule body) {
        try {
            return ApiResponse.success(commissionService.updateRule(merchantId, body));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    /**
     * 结清：生成结算单快照并标记明细已结
     * body: periodType, periodStart(yyyy-MM-dd), periodEnd(CUSTOM时), voucherNo, remark
     */
    @PostMapping("/merchants/{merchantId}/settle")
    public ApiResponse<PlatformCommissionSettlement> settle(@PathVariable("merchantId") Long merchantId,
                                                            @RequestBody Map<String, Object> body) {
        try {
            String periodType = body.get("periodType") == null ? "CUSTOM" : body.get("periodType").toString();
            LocalDate start = parseDate(body.get("periodStart"));
            LocalDate end = parseDate(body.get("periodEnd"));
            String voucherNo = body.get("voucherNo") == null ? null : body.get("voucherNo").toString();
            String remark = body.get("remark") == null ? null : body.get("remark").toString();
            return ApiResponse.success(commissionService.settle(merchantId, periodType, start, end, voucherNo, remark));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("结算失败", e);
            return ApiResponse.error("结算失败");
        }
    }

    @GetMapping("/settlements")
    public ApiResponse<List<PlatformCommissionSettlement>> settlements(
            @RequestParam(value = "merchantId", required = false) Long merchantId) {
        return ApiResponse.success(commissionService.listSettlements(merchantId));
    }

    @GetMapping("/settlements/{id}")
    public ApiResponse<PlatformCommissionSettlement> settlementDetail(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success(commissionService.getSettlement(id));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    private LocalDate parseDate(Object v) {
        if (v == null || !org.springframework.util.StringUtils.hasText(v.toString())) {
            return null;
        }
        return LocalDate.parse(v.toString());
    }
}
