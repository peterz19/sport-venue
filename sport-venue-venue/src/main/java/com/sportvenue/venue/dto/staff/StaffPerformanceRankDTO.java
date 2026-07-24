package com.sportvenue.venue.dto.staff;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class StaffPerformanceRankDTO {
    private String dateFrom;
    private String dateTo;
    private List<StaffPerformanceDTO> items;
}
