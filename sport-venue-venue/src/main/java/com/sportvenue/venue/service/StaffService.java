package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.*;

import java.util.List;
import java.util.Map;

public interface StaffService {
    ApiResponse<List<StaffDTO>> listStaff();

    ApiResponse<List<StaffDTO>> listOptions();

    ApiResponse<StaffDTO> createStaff(StaffCreateRequest request);

    ApiResponse<StaffDTO> updateStaff(Long id, StaffUpdateRequest request);

    ApiResponse<Void> updateStatus(Long id, String status);

    ApiResponse<Void> resetPassword(Long id, Map<String, String> body);
}
