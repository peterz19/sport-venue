package com.sportvenue.venue.dto.staff;

import lombok.Data;

@Data
public class StaffCreateRequest {
    private String username;
    private String password;
    private String realName;
    private String phone;
    private String remark;
}
