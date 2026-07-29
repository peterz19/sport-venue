package com.sportvenue.venue.dto.merchant;

import lombok.Data;

@Data
public class MerchantUpdateRequest {
    private String name;
    private String shortName;
    private String merchantType;
    private String contactName;
    private String contactPhone;
    private String contactEmail;
    private String address;
    private String businessLicense;
    private String businessHours;
    private String description;
    private String remark;
    private String status;
}
