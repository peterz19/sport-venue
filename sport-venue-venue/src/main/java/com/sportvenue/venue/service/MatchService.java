package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.match.MatchResultDTO;
import com.sportvenue.venue.dto.match.MatchSaveRequest;
import com.sportvenue.venue.dto.match.TeamRankingDTO;

import java.util.List;

public interface MatchService {
    ApiResponse<List<MatchResultDTO>> list();
    ApiResponse<MatchResultDTO> create(MatchSaveRequest request);
    ApiResponse<MatchResultDTO> update(Long id, MatchSaveRequest request);
    ApiResponse<List<TeamRankingDTO>> ranking();
}
