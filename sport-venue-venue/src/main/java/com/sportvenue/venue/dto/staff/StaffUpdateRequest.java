package com.sportvenue.venue.dto.staff;

import lombok.Data;

@Data
public class StaffUpdateRequest {
    private String realName;
    private String phone;
    private String remark;
}
