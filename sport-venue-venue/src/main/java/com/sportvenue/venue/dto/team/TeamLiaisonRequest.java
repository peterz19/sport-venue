package com.sportvenue.venue.dto.team;

import lombok.Data;

@Data
public class TeamLiaisonRequest {
    private Long liaisonStaffId;
    private String reason;
}
