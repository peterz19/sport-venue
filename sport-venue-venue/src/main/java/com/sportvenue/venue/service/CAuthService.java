package com.sportvenue.venue.service;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.venue.config.JwtConfig;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.util.SecretCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class CAuthService {

    @Autowired
    private MerchantWxChannelRepository wxChannelRepository;
    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private MerchantFeatureService featureService;
    @Autowired
    private UserWxIdentityRepository identityRepository;
    @Autowired
    private CustomerUserRepository customerUserRepository;
    @Autowired
    private CustomerWalletRepository walletRepository;
    @Autowired
    private SecretCrypto secretCrypto;
    @Autowired
    private JwtConfig jwtConfig;

    @Value("${saas.wx.mock-enabled:true}")
    private boolean mockEnabled;

    @Transactional
    public Map<String, Object> wxLogin(String appId, String code) {
        if (!StringUtils.hasText(appId) || !StringUtils.hasText(code)) {
            throw new BusinessException(400, "appId 与 code 必填");
        }
        MerchantWxChannel channel = wxChannelRepository.findByAppId(appId.trim())
                .orElseThrow(() -> new BusinessException(404, "未配置的小程序 AppId"));
        if (channel.getChannelType() != MerchantWxChannel.ChannelType.MINI_PROGRAM) {
            throw new BusinessException(400, "AppId 不是小程序渠道");
        }
        if (channel.getBindStatus() != MerchantWxChannel.BindStatus.BOUND) {
            throw new BusinessException(400, "微信渠道未绑定完成");
        }
        Merchant merchant = merchantRepository.findById(channel.getMerchantId())
                .orElseThrow(() -> new BusinessException(40301, "商户不存在或已停用"));
        if (merchant.getStatus() != Merchant.MerchantStatus.ACTIVE) {
            throw new BusinessException(40301, "商户已停用，请联系平台");
        }
        featureService.requireEnabled(merchant.getId(), MerchantFeatureService.Feature.C_END);

        String openid = resolveOpenid(channel, code.trim());
        UserWxIdentity identity = identityRepository.findByAppIdAndOpenid(appId.trim(), openid)
                .orElse(null);
        CustomerUser customer;
        if (identity == null) {
            customer = new CustomerUser();
            customer.setMerchantId(merchant.getId());
            customer.setNickname("微信用户");
            customer.setStatus(CustomerUser.Status.ACTIVE);
            customer = customerUserRepository.save(customer);

            identity = new UserWxIdentity();
            identity.setMerchantId(merchant.getId());
            identity.setChannelType(channel.getChannelType().name());
            identity.setAppId(appId.trim());
            identity.setOpenid(openid);
            identity.setCustomerUserId(customer.getId());
            identityRepository.save(identity);

            CustomerWallet wallet = new CustomerWallet();
            wallet.setMerchantId(merchant.getId());
            wallet.setCustomerUserId(customer.getId());
            wallet.setBalance(BigDecimal.ZERO);
            wallet.setUpdateTime(LocalDateTime.now());
            walletRepository.save(wallet);
        } else {
            customer = customerUserRepository.findById(identity.getCustomerUserId())
                    .orElseThrow(() -> new BusinessException(404, "顾客不存在"));
            if (customer.getStatus() != CustomerUser.Status.ACTIVE) {
                throw new BusinessException(40302, "账号已停用");
            }
        }

        String subject = "c_" + customer.getId();
        String token = jwtConfig.generateToken(subject, customer.getId(), "C_USER");

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("customerId", customer.getId());
        result.put("merchantId", merchant.getId());
        result.put("merchantName", merchant.getName());
        result.put("nickname", customer.getNickname());
        result.put("expiresIn", 86400);
        return result;
    }

    private String resolveOpenid(MerchantWxChannel channel, String code) {
        if (mockEnabled) {
            // 本地/联调：openid = mock_{code}，不调微信
            return "mock_" + code;
        }
        String secret = secretCrypto.decrypt(channel.getAppSecretEnc());
        if (!StringUtils.hasText(secret)) {
            throw new BusinessException(500, "小程序 Secret 未配置");
        }
        // 真实 code2session 可在此接入；当前未开 mock 时仍回退 mock 便于联调
        log.warn("saas.wx.mock-enabled=false 但未接入真实微信，仍使用 mock openid");
        return "mock_" + code;
    }
}
