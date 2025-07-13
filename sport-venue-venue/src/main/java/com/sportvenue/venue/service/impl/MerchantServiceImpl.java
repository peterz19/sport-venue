package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.Merchant;
import com.sportvenue.venue.repository.MerchantRepository;
import com.sportvenue.venue.service.MerchantService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商户服务实现类
 */
@Slf4j
@Service
@Transactional
public class MerchantServiceImpl implements MerchantService {
    
    @Autowired
    private MerchantRepository merchantRepository;
    
    @Override
    public ApiResponse<List<Merchant>> getMerchants() {
        try {
            List<Merchant> merchants = merchantRepository.findAll();
            return ApiResponse.success(merchants);
        } catch (Exception e) {
            log.error("获取商户列表异常：", e);
            return ApiResponse.error("获取商户列表失败");
        }
    }
    
    @Override
    public ApiResponse<Merchant> getMerchantById(Long id) {
        try {
            Merchant merchant = merchantRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("商户不存在"));
            return ApiResponse.success(merchant);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getMessage());
        } catch (Exception e) {
            log.error("获取商户详情异常：", e);
            return ApiResponse.error("获取商户详情失败");
        }
    }
} 