package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.StaffPerformanceDTO;
import com.sportvenue.venue.dto.staff.StaffPerformanceRankDTO;
import com.sportvenue.venue.service.PerformanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Tag(name = "B端员工业绩", description = "收银业绩（订场指标预留）")
@Slf4j
@RestController
@RequestMapping("/business/performance")
@CrossOrigin(origins = "*")
public class BusinessPerformanceController {

    @Autowired
    private PerformanceService performanceService;

    @Operation(summary = "我的业绩")
    @GetMapping("/me")
    public ApiResponse<StaffPerformanceDTO> me(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo) {
        return performanceService.myPerformance(date, dateFrom, dateTo);
    }

    @Operation(summary = "员工业绩排行（老板）")
    @GetMapping("/staff")
    public ApiResponse<StaffPerformanceRankDTO> rank(
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo) {
        return performanceService.staffRank(date, dateFrom, dateTo);
    }

    @Operation(summary = "指定员工业绩")
    @GetMapping("/staff/{id}")
    public ApiResponse<StaffPerformanceDTO> one(
            @PathVariable("id") Long id,
            @RequestParam(value = "date", required = false) String date,
            @RequestParam(value = "dateFrom", required = false) String dateFrom,
            @RequestParam(value = "dateTo", required = false) String dateTo) {
        return performanceService.staffPerformance(id, date, dateFrom, dateTo);
    }
}
