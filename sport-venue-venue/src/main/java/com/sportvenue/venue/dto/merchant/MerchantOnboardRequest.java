package com.sportvenue.venue.dto.merchant;

import lombok.Data;

@Data
public class MerchantOnboardRequest {
    /** 商户编码，可空自动生成 */
    private String merchantCode;
    private String name;
    private String shortName;
    /** INDIVIDUAL / COMPANY / CHAIN */
    private String merchantType;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String businessLicense;
    private String remark;

    /** 老板登录账号 */
    private String ownerUsername;
    private String ownerPassword;
    private String ownerRealName;
    private String ownerPhone;

    /** 可选首场馆 */
    private FirstVenue firstVenue;

    @Data
    public static class FirstVenue {
        private String name;
        private String type;
        private String spaceType;
        private String chargeType;
        private String address;
        private Integer capacity;
        private String openTime;
        private String closeTime;
        private String phone;
        private String description;
    }
}
