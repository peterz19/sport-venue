package com.sportvenue.venue.dto.staff;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StaffDTO {
    private Long id;
    private String username;
    private String realName;
    private String phone;
    private String userType;
    private String role;
    private String status;
    private String remark;
    private Long merchantId;
    private String merchantName;
}
