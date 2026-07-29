package com.sportvenue.venue.controller;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.service.CAuthService;
import com.sportvenue.venue.service.CBookingService;
import com.sportvenue.venue.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "C端接口")
@Slf4j
@RestController
@RequestMapping("/c")
@CrossOrigin(origins = "*")
public class CEndController {

    @Autowired
    private CAuthService cAuthService;
    @Autowired
    private CBookingService cBookingService;
    @Autowired
    private WalletService walletService;

    @PostMapping("/auth/wx-login")
    public ApiResponse<Map<String, Object>> wxLogin(@RequestBody Map<String, String> body) {
        try {
            return ApiResponse.success(cAuthService.wxLogin(body.get("appId"), body.get("code")));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("C端登录失败", e);
            return ApiResponse.error("登录失败");
        }
    }

    @GetMapping("/venues")
    public ApiResponse<List<Map<String, Object>>> venues() {
        try {
            return ApiResponse.success(cBookingService.listVenues());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/courts")
    public ApiResponse<List<Map<String, Object>>> courts(@RequestParam(value = "venueId", required = false) Long venueId) {
        try {
            return ApiResponse.success(cBookingService.listCourts(venueId));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/courts/{id}/slots")
    public ApiResponse<Map<String, Object>> slots(@PathVariable("id") Long id,
                                                  @RequestParam("date") String date) {
        try {
            return ApiResponse.success(cBookingService.slots(id, date));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/bookings")
    public ApiResponse<Map<String, Object>> createBooking(@RequestBody Map<String, Object> body) {
        try {
            return ApiResponse.success(cBookingService.create(body));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("C端订场失败", e);
            return ApiResponse.error("订场失败");
        }
    }

    @GetMapping("/bookings/mine")
    public ApiResponse<List<Map<String, Object>>> myBookings() {
        try {
            return ApiResponse.success(cBookingService.mine());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public ApiResponse<Map<String, Object>> cancel(@PathVariable("id") Long id) {
        try {
            return ApiResponse.success(cBookingService.cancel(id));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/wallet")
    public ApiResponse<Map<String, Object>> wallet() {
        try {
            return ApiResponse.success(walletService.myWallet());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @GetMapping("/wallet/ledger")
    public ApiResponse<?> ledger() {
        try {
            return ApiResponse.success(walletService.myLedgers());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }

    @PostMapping("/wallet/recharge")
    public ApiResponse<Map<String, Object>> recharge(@RequestBody Map<String, Object> body) {
        try {
            BigDecimal amount = new BigDecimal(body.get("amount").toString());
            return ApiResponse.success(walletService.createRecharge(amount));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("充值失败", e);
            return ApiResponse.error("充值失败");
        }
    }

    /** mock 支付回调（幂等确认） */
    @PostMapping("/pay/notify/mock")
    public ApiResponse<Void> mockPayNotify(@RequestBody Map<String, String> body) {
        try {
            walletService.confirmPaid(body.get("orderNo"));
            return ApiResponse.success(null);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        }
    }
}
