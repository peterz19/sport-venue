package com.sportvenue.venue.dto.match;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeamRankingDTO {
    private Long teamId;
    private String teamName;
    private Integer played;
    private Integer win;
    private Integer draw;
    private Integer loss;
    private Integer pointsFor;
    private Integer pointsAgainst;
    private Integer pointDiff;
    private Integer rankingPoints;
}
