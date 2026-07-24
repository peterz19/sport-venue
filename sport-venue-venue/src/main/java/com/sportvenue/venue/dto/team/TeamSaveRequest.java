package com.sportvenue.venue.dto.team;

import lombok.Data;

@Data
public class TeamSaveRequest {
    private String name;
    private String captainName;
    private String phone;
    private String remark;
    private Long liaisonStaffId;
}
