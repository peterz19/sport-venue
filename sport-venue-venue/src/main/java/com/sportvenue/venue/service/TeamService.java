package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.team.*;

import java.util.List;

public interface TeamService {
    ApiResponse<List<TeamDTO>> list();
    ApiResponse<List<TeamDTO>> options();
    ApiResponse<TeamDetailDTO> detail(Long id);
    ApiResponse<TeamDTO> create(TeamSaveRequest request);
    ApiResponse<TeamDTO> update(Long id, TeamSaveRequest request);
    ApiResponse<TeamDTO> changeLiaison(Long id, TeamLiaisonRequest request);
    ApiResponse<Void> updateStatus(Long id, String status);
    ApiResponse<List<TeamAuditDTO>> audits(Long id);
}
