package com.sportvenue.venue.dto.match;

import lombok.Data;

@Data
public class MatchSaveRequest {
    private Long bookingId;
    private Long homeTeamId;
    private Long awayTeamId;
    private Integer homeScore;
    private Integer awayScore;
    private String remark;
}
