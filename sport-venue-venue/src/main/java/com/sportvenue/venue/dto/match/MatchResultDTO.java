package com.sportvenue.venue.dto.match;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchResultDTO {
    private Long id;
    private Long bookingId;
    private String bookingOrderNo;
    private Long homeTeamId;
    private String homeTeamName;
    private Long awayTeamId;
    private String awayTeamName;
    private Integer homeScore;
    private Integer awayScore;
    private String result;
    private Long operatorId;
    private String operatorName;
    private String remark;
    private String createTime;
}
