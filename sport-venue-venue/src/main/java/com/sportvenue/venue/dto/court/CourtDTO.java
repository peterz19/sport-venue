package com.sportvenue.venue.dto.court;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CourtDTO {
    private Long id;
    private Long merchantId;
    private Long venueId;
    private String venueName;
    private String name;
    private String code;
    private String courtType;
    private String status;
    private Integer sortOrder;
    private String remark;
}
