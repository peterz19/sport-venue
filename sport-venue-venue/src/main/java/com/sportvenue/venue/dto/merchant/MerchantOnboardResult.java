package com.sportvenue.venue.dto.merchant;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MerchantOnboardResult {
    private Long merchantId;
    private String merchantCode;
    private String merchantName;
    private Long ownerUserId;
    private String ownerUsername;
    private Long firstVenueId;
}
