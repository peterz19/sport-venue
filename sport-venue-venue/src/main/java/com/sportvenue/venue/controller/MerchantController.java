package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.Merchant;
import com.sportvenue.venue.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商户控制器
 */
@Slf4j
@RestController
@RequestMapping("/merchants")
public class MerchantController {
    
    @Autowired
    private MerchantService merchantService;
    
    /**
     * 获取商户列表
     */
    @GetMapping
    public ApiResponse<List<Merchant>> getMerchants() {
        log.info("获取商户列表请求");
        return merchantService.getMerchants();
    }
    
    /**
     * 根据ID获取商户详情
     */
    @GetMapping("/{id}")
    public ApiResponse<Merchant> getMerchantById(@PathVariable("id") Long id) {
        log.info("获取商户详情请求，商户ID：{}", id);
        return merchantService.getMerchantById(id);
    }
} 