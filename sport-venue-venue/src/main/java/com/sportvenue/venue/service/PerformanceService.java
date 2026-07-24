package com.sportvenue.venue.service;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.StaffPerformanceDTO;
import com.sportvenue.venue.dto.staff.StaffPerformanceRankDTO;

public interface PerformanceService {
    ApiResponse<StaffPerformanceDTO> myPerformance(String date, String dateFrom, String dateTo);

    ApiResponse<StaffPerformanceRankDTO> staffRank(String date, String dateFrom, String dateTo);

    ApiResponse<StaffPerformanceDTO> staffPerformance(Long staffId, String date, String dateFrom, String dateTo);
}
