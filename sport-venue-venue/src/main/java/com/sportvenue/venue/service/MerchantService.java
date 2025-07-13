package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.Merchant;

import java.util.List;

/**
 * 商户服务接口
 */
public interface MerchantService {
    
    /**
     * 获取商户列表
     */
    ApiResponse<List<Merchant>> getMerchants();
    
    /**
     * 根据ID获取商户详情
     */
    ApiResponse<Merchant> getMerchantById(Long id);
} 