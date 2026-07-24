package com.sportvenue.venue.dto.court;

import lombok.Data;

@Data
public class CourtSaveRequest {
    private Long venueId;
    private String name;
    private String code;
    private String courtType;
    private Integer sortOrder;
    private String remark;
    private String status;
}
