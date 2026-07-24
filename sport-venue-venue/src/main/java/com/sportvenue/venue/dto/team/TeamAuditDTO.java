package com.sportvenue.venue.dto.team;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamAuditDTO {
    private Long id;
    private Long teamId;
    private String action;
    private String beforeJson;
    private String afterJson;
    private String reason;
    private Long operatorId;
    private String operatorName;
    private String createTime;
}
