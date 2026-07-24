package com.sportvenue.venue.dto.team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamDTO {
    private Long id;
    private Long merchantId;
    private String name;
    private String captainName;
    private String phone;
    private String remark;
    private Long liaisonStaffId;
    private String liaisonStaffName;
    private String status;
}
