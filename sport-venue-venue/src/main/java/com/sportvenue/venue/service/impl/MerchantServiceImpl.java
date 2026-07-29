package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.merchant.*;
import com.sportvenue.venue.entity.*;
import com.sportvenue.venue.repository.*;
import com.sportvenue.venue.service.MerchantFeatureService;
import com.sportvenue.venue.service.MerchantService;
import com.sportvenue.venue.service.PlatformAuditService;
import com.sportvenue.venue.service.PlatformCommissionService;
import com.sportvenue.venue.util.SecretCrypto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MerchantServiceImpl implements MerchantService {

    @Autowired
    private MerchantRepository merchantRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private VenueRepository venueRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private MerchantFeatureService featureService;
    @Autowired
    private PlatformAuditService auditService;
    @Autowired
    private PlatformAuditLogRepository auditLogRepository;
    @Autowired
    private MerchantWxChannelRepository wxChannelRepository;
    @Autowired
    private MerchantWxPayRepository wxPayRepository;
    @Autowired
    private SecretCrypto secretCrypto;
    @Autowired
    private PlatformCommissionService platformCommissionService;

    @Override
    public ApiResponse<List<Merchant>> getMerchants() {
        try {
            return ApiResponse.success(merchantRepository.findAll());
        } catch (Exception e) {
            log.error("获取商户列表异常：", e);
            return ApiResponse.error("获取商户列表失败");
        }
    }

    @Override
    public ApiResponse<Merchant> getMerchantById(Long id) {
        try {
            Merchant merchant = merchantRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "商户不存在"));
            return ApiResponse.success(merchant);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("获取商户详情异常：", e);
            return ApiResponse.error("获取商户详情失败");
        }
    }

    @Override
    public ApiResponse<MerchantOnboardResult> onboard(MerchantOnboardRequest request) {
        try {
            if (!StringUtils.hasText(request.getName())) {
                throw new BusinessException(400, "商户名称不能为空");
            }
            if (!StringUtils.hasText(request.getOwnerUsername()) || !StringUtils.hasText(request.getOwnerPassword())) {
                throw new BusinessException(400, "老板账号和密码不能为空");
            }
            if (request.getOwnerPassword().length() < 6) {
                throw new BusinessException(400, "老板密码至少6位");
            }
            if (!StringUtils.hasText(request.getOwnerRealName())) {
                throw new BusinessException(400, "老板姓名不能为空");
            }
            if (userRepository.existsByUsername(request.getOwnerUsername().trim())) {
                throw new BusinessException(400, "老板用户名已存在");
            }

            String code = StringUtils.hasText(request.getMerchantCode())
                    ? request.getMerchantCode().trim()
                    : "M" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
            if (merchantRepository.existsByMerchantCode(code)) {
                throw new BusinessException(400, "商户编码已存在");
            }

            Merchant merchant = new Merchant();
            merchant.setMerchantCode(code);
            merchant.setName(request.getName().trim());
            merchant.setShortName(request.getShortName());
            merchant.setMerchantType(parseType(request.getMerchantType()));
            merchant.setStatus(Merchant.MerchantStatus.ACTIVE);
            merchant.setContactName(request.getContactName());
            merchant.setContactPhone(request.getContactPhone());
            merchant.setContactEmail(request.getContactEmail());
            merchant.setAddress(request.getAddress());
            merchant.setBusinessLicense(request.getBusinessLicense());
            merchant.setRemark(request.getRemark());
            merchant.setVenueCount(0);
            Merchant saved = merchantRepository.save(merchant);

            User owner = new User();
            owner.setUsername(request.getOwnerUsername().trim());
            owner.setPassword(passwordEncoder.encode(request.getOwnerPassword()));
            owner.setRealName(request.getOwnerRealName().trim());
            owner.setPhone(StringUtils.hasText(request.getOwnerPhone()) ? request.getOwnerPhone().trim() : null);
            owner.setUserType(User.UserType.B_MERCHANT);
            owner.setMerchantId(saved.getId());
            owner.setMerchantName(saved.getName());
            owner.setStatus(User.UserStatus.ACTIVE);
            owner.setPoints(0);
            owner.setCreateTime(LocalDateTime.now());
            owner.setUpdateTime(LocalDateTime.now());
            User savedOwner = userRepository.save(owner);

            featureService.getOrCreate(saved.getId());
            platformCommissionService.ensureDefaultRule(saved.getId());

            Long venueId = null;
            if (request.getFirstVenue() != null && StringUtils.hasText(request.getFirstVenue().getName())) {
                venueId = createFirstVenue(saved, request.getFirstVenue());
                saved.setVenueCount(1);
                merchantRepository.save(saved);
            }

            auditService.record("MERCHANT_ONBOARD", "MERCHANT", saved.getId(), saved.getId(),
                    null,
                    "{\"merchantCode\":\"" + saved.getMerchantCode()
                            + "\",\"ownerUsername\":\"" + savedOwner.getUsername() + "\"}",
                    "开户");

            return ApiResponse.success(MerchantOnboardResult.builder()
                    .merchantId(saved.getId())
                    .merchantCode(saved.getMerchantCode())
                    .merchantName(saved.getName())
                    .ownerUserId(savedOwner.getId())
                    .ownerUsername(savedOwner.getUsername())
                    .firstVenueId(venueId)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("商户开户失败", e);
            return ApiResponse.error("商户开户失败");
        }
    }

    private Long createFirstVenue(Merchant merchant, MerchantOnboardRequest.FirstVenue fv) {
        if (!StringUtils.hasText(fv.getAddress())) {
            throw new BusinessException(400, "首场馆地址不能为空");
        }
        Venue venue = new Venue();
        venue.setName(fv.getName().trim());
        venue.setMerchantId(merchant.getId());
        venue.setMerchantName(merchant.getName());
        venue.setAddress(fv.getAddress().trim());
        venue.setType(Venue.VenueType.valueOf(
                StringUtils.hasText(fv.getType()) ? fv.getType() : "GYM"));
        venue.setSpaceType(Venue.VenueSpaceType.valueOf(
                StringUtils.hasText(fv.getSpaceType()) ? fv.getSpaceType() : "INDOOR"));
        venue.setChargeType(Venue.VenueChargeType.valueOf(
                StringUtils.hasText(fv.getChargeType()) ? fv.getChargeType() : "PAID"));
        venue.setCapacity(fv.getCapacity());
        venue.setOpenTime(StringUtils.hasText(fv.getOpenTime()) ? fv.getOpenTime() : "08:00");
        venue.setCloseTime(StringUtils.hasText(fv.getCloseTime()) ? fv.getCloseTime() : "22:00");
        venue.setPhone(fv.getPhone());
        venue.setDescription(fv.getDescription());
        venue.setStatus(Venue.VenueStatus.ACTIVE);
        return venueRepository.save(venue).getId();
    }

    @Override
    public ApiResponse<Merchant> update(Long id, MerchantUpdateRequest request) {
        try {
            Merchant merchant = merchantRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "商户不存在"));
            String before = merchant.getName();
            if (StringUtils.hasText(request.getName())) {
                merchant.setName(request.getName().trim());
            }
            if (request.getShortName() != null) {
                merchant.setShortName(request.getShortName());
            }
            if (StringUtils.hasText(request.getMerchantType())) {
                merchant.setMerchantType(parseType(request.getMerchantType()));
            }
            if (request.getContactName() != null) merchant.setContactName(request.getContactName());
            if (request.getContactPhone() != null) merchant.setContactPhone(request.getContactPhone());
            if (request.getContactEmail() != null) merchant.setContactEmail(request.getContactEmail());
            if (request.getAddress() != null) merchant.setAddress(request.getAddress());
            if (request.getBusinessLicense() != null) merchant.setBusinessLicense(request.getBusinessLicense());
            if (request.getBusinessHours() != null) merchant.setBusinessHours(request.getBusinessHours());
            if (request.getDescription() != null) merchant.setDescription(request.getDescription());
            if (request.getRemark() != null) merchant.setRemark(request.getRemark());
            if (StringUtils.hasText(request.getStatus())) {
                merchant.setStatus(Merchant.MerchantStatus.valueOf(request.getStatus()));
            }
            Merchant saved = merchantRepository.save(merchant);
            auditService.record("MERCHANT_UPDATE", "MERCHANT", id, id,
                    "{\"name\":\"" + before + "\"}",
                    "{\"name\":\"" + saved.getName() + "\"}", null);
            return ApiResponse.success(saved);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "状态或类型不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新商户失败", e);
            return ApiResponse.error("更新商户失败");
        }
    }

    @Override
    public ApiResponse<Void> updateStatus(Long id, String status) {
        try {
            Merchant merchant = merchantRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "商户不存在"));
            String before = merchant.getStatus().name();
            merchant.setStatus(Merchant.MerchantStatus.valueOf(status));
            merchantRepository.save(merchant);
            auditService.record("MERCHANT_STATUS", "MERCHANT", id, id,
                    "{\"status\":\"" + before + "\"}",
                    "{\"status\":\"" + status + "\"}",
                    "停用后商户账号将无法登录");
            return ApiResponse.success(null);
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "状态不合法");
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新商户状态失败", e);
            return ApiResponse.error("更新商户状态失败");
        }
    }

    @Override
    public ApiResponse<MerchantOverviewDTO> overview(Long id) {
        try {
            Merchant merchant = merchantRepository.findById(id)
                    .orElseThrow(() -> new BusinessException(404, "商户不存在"));
            User owner = userRepository.findByMerchantId(id).stream()
                    .filter(u -> u.getUserType() == User.UserType.B_MERCHANT)
                    .findFirst().orElse(null);
            long venueCount = venueRepository.findByMerchantId(id).size();
            long staffCount = userRepository.findByMerchantId(id).stream()
                    .filter(u -> u.getUserType() == User.UserType.B_MERCHANT
                            || u.getUserType() == User.UserType.B_STAFF)
                    .count();

            List<MerchantWxChannel> channels = wxChannelRepository.findByMerchantId(id);
            MerchantWxChannel mini = channels.stream()
                    .filter(c -> c.getChannelType() == MerchantWxChannel.ChannelType.MINI_PROGRAM)
                    .findFirst().orElse(null);
            MerchantWxChannel oa = channels.stream()
                    .filter(c -> c.getChannelType() == MerchantWxChannel.ChannelType.OFFICIAL_ACCOUNT)
                    .findFirst().orElse(null);

            List<Map<String, Object>> channelViews = channels.stream().map(this::maskChannel).collect(Collectors.toList());

            return ApiResponse.success(MerchantOverviewDTO.builder()
                    .merchantId(merchant.getId())
                    .merchantCode(merchant.getMerchantCode())
                    .name(merchant.getName())
                    .status(merchant.getStatus().name())
                    .merchantType(merchant.getMerchantType().name())
                    .contactName(merchant.getContactName())
                    .contactPhone(merchant.getContactPhone())
                    .address(merchant.getAddress())
                    .ownerUserId(owner == null ? null : owner.getId())
                    .ownerUsername(owner == null ? null : owner.getUsername())
                    .ownerRealName(owner == null ? null : owner.getRealName())
                    .ownerStatus(owner == null ? null : owner.getStatus().name())
                    .venueCount(venueCount)
                    .staffCount(staffCount)
                    .wxMiniBound(mini != null && mini.getBindStatus() == MerchantWxChannel.BindStatus.BOUND)
                    .wxOaBound(oa != null && oa.getBindStatus() == MerchantWxChannel.BindStatus.BOUND)
                    .wxMiniAppId(mini == null ? null : mini.getAppId())
                    .wxOaAppId(oa == null ? null : oa.getAppId())
                    .features(featureService.getOrCreate(id))
                    .recentAudits(auditLogRepository.findTop50ByMerchantIdOrderByCreateTimeDesc(id))
                    .wxChannels(channelViews)
                    .build());
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("商户概览失败", e);
            return ApiResponse.error("商户概览失败");
        }
    }

    @Override
    public ApiResponse<List<PlatformAuditLog>> audits(Long id) {
        return ApiResponse.success(auditLogRepository.findTop50ByMerchantIdOrderByCreateTimeDesc(id));
    }

    @Override
    public ApiResponse<MerchantFeatures> getFeatures(Long id) {
        ensureMerchant(id);
        return ApiResponse.success(featureService.getOrCreate(id));
    }

    @Override
    public ApiResponse<MerchantFeatures> updateFeatures(Long id, MerchantFeatures patch) {
        ensureMerchant(id);
        MerchantFeatures saved = featureService.update(id, patch);
        auditService.record("MERCHANT_FEATURES", "MERCHANT", id, id, null, null, "更新功能开关");
        return ApiResponse.success(saved);
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> listWxChannels(Long id) {
        ensureMerchant(id);
        return ApiResponse.success(wxChannelRepository.findByMerchantId(id).stream()
                .map(this::maskChannel).collect(Collectors.toList()));
    }

    @Override
    public ApiResponse<Map<String, Object>> upsertWxChannel(Long id, WxChannelUpsertRequest request) {
        try {
            ensureMerchant(id);
            if (!StringUtils.hasText(request.getChannelType()) || !StringUtils.hasText(request.getAppId())) {
                throw new BusinessException(400, "channelType 与 appId 必填");
            }
            MerchantWxChannel.ChannelType type = MerchantWxChannel.ChannelType.valueOf(request.getChannelType());
            if (wxChannelRepository.existsByAppIdAndMerchantIdNot(request.getAppId().trim(), id)) {
                throw new BusinessException(400, "AppId 已被其他商户占用");
            }
            MerchantWxChannel channel = wxChannelRepository
                    .findByMerchantIdAndChannelType(id, type)
                    .orElseGet(MerchantWxChannel::new);
            channel.setMerchantId(id);
            channel.setChannelType(type);
            channel.setAppId(request.getAppId().trim());
            if (StringUtils.hasText(request.getAppSecret())) {
                channel.setAppSecretEnc(secretCrypto.encrypt(request.getAppSecret().trim()));
            }
            if (request.getOaServerToken() != null) {
                channel.setOaServerToken(request.getOaServerToken());
            }
            if (request.getOaEncodingAesKey() != null) {
                channel.setOaEncodingAesKey(request.getOaEncodingAesKey());
            }
            channel.setRemark(request.getRemark());
            channel.setBindStatus(secretCrypto.hasSecret(channel.getAppSecretEnc())
                    ? MerchantWxChannel.BindStatus.BOUND
                    : MerchantWxChannel.BindStatus.UNSET);
            channel.setAuthType(MerchantWxChannel.AuthType.SELF);
            MerchantWxChannel saved = wxChannelRepository.save(channel);
            auditService.record("MERCHANT_WX_BIND", "MERCHANT", id, id, null,
                    "{\"channelType\":\"" + type + "\",\"appId\":\"" + saved.getAppId() + "\"}", null);
            return ApiResponse.success(maskChannel(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (IllegalArgumentException e) {
            return ApiResponse.error(400, "channelType 不合法");
        } catch (Exception e) {
            log.error("保存微信渠道失败", e);
            return ApiResponse.error("保存微信渠道失败");
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getWxPay(Long id) {
        ensureMerchant(id);
        return ApiResponse.success(wxPayRepository.findById(id).map(this::maskPay).orElseGet(HashMap::new));
    }

    @Override
    public ApiResponse<Map<String, Object>> upsertWxPay(Long id, Map<String, String> body) {
        try {
            ensureMerchant(id);
            MerchantWxPay pay = wxPayRepository.findById(id).orElseGet(MerchantWxPay::new);
            pay.setMerchantId(id);
            if (body.get("mchId") != null) {
                pay.setMchId(body.get("mchId"));
            }
            if (StringUtils.hasText(body.get("mchApiV3Key"))) {
                pay.setMchApiV3KeyEnc(secretCrypto.encrypt(body.get("mchApiV3Key")));
            }
            if (body.get("mchSerialNo") != null) {
                pay.setMchSerialNo(body.get("mchSerialNo"));
            }
            if (body.get("notifyPath") != null) {
                pay.setNotifyPath(body.get("notifyPath"));
            }
            if (StringUtils.hasText(body.get("status"))) {
                pay.setStatus(MerchantWxPay.PayStatus.valueOf(body.get("status")));
            } else if (StringUtils.hasText(pay.getMchId())) {
                pay.setStatus(MerchantWxPay.PayStatus.ACTIVE);
            }
            pay.setUpdateTime(LocalDateTime.now());
            MerchantWxPay saved = wxPayRepository.save(pay);
            auditService.record("MERCHANT_WX_PAY", "MERCHANT", id, id, null,
                    "{\"mchId\":\"" + saved.getMchId() + "\"}", null);
            return ApiResponse.success(maskPay(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("保存微信支付配置失败", e);
            return ApiResponse.error("保存微信支付配置失败");
        }
    }

    private Map<String, Object> maskChannel(MerchantWxChannel c) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("id", c.getId());
        m.put("merchantId", c.getMerchantId());
        m.put("channelType", c.getChannelType().name());
        m.put("appId", c.getAppId());
        m.put("hasSecret", secretCrypto.hasSecret(c.getAppSecretEnc()));
        m.put("bindStatus", c.getBindStatus().name());
        m.put("authType", c.getAuthType().name());
        m.put("oaServerToken", c.getOaServerToken());
        m.put("remark", c.getRemark());
        return m;
    }

    private Map<String, Object> maskPay(MerchantWxPay p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("merchantId", p.getMerchantId());
        m.put("mchId", p.getMchId());
        m.put("hasApiKey", secretCrypto.hasSecret(p.getMchApiV3KeyEnc()));
        m.put("mchSerialNo", p.getMchSerialNo());
        m.put("notifyPath", p.getNotifyPath());
        m.put("status", p.getStatus() == null ? null : p.getStatus().name());
        return m;
    }

    private void ensureMerchant(Long id) {
        if (!merchantRepository.existsById(id)) {
            throw new BusinessException(404, "商户不存在");
        }
    }

    private Merchant.MerchantType parseType(String type) {
        if (!StringUtils.hasText(type)) {
            return Merchant.MerchantType.COMPANY;
        }
        return Merchant.MerchantType.valueOf(type);
    }
}
