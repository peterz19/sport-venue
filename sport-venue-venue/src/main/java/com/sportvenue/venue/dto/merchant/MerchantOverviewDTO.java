package com.sportvenue.venue.dto.merchant;

import com.sportvenue.venue.entity.MerchantFeatures;
import com.sportvenue.venue.entity.PlatformAuditLog;
import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class MerchantOverviewDTO {
    private Long merchantId;
    private String merchantCode;
    private String name;
    private String status;
    private String merchantType;
    private String contactName;
    private String contactPhone;
    private String address;

    private Long ownerUserId;
    private String ownerUsername;
    private String ownerRealName;
    private String ownerStatus;

    private long venueCount;
    private long staffCount;

    private boolean wxMiniBound;
    private boolean wxOaBound;
    private String wxMiniAppId;
    private String wxOaAppId;

    private MerchantFeatures features;
    private List<PlatformAuditLog> recentAudits;
    private List<Map<String, Object>> wxChannels;
}
