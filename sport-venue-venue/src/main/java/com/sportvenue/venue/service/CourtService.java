package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.court.CourtDTO;
import com.sportvenue.venue.dto.court.CourtSaveRequest;

import java.util.List;

public interface CourtService {
    ApiResponse<List<CourtDTO>> list(Long venueId);
    ApiResponse<List<CourtDTO>> options(Long venueId);
    ApiResponse<CourtDTO> create(CourtSaveRequest request);
    ApiResponse<CourtDTO> update(Long id, CourtSaveRequest request);
    ApiResponse<Void> updateStatus(Long id, String status);
}
