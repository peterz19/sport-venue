package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.court.CourtDTO;
import com.sportvenue.venue.dto.court.CourtSaveRequest;
import com.sportvenue.venue.service.CourtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "B端片场管理")
@RestController
@RequestMapping("/business/courts")
@CrossOrigin(origins = "*")
public class BusinessCourtController {

    @Autowired
    private CourtService courtService;

    @Operation(summary = "片场列表")
    @GetMapping
    public ApiResponse<List<CourtDTO>> list(@RequestParam(value = "venueId", required = false) Long venueId) {
        return courtService.list(venueId);
    }

    @Operation(summary = "在用片场下拉")
    @GetMapping("/options")
    public ApiResponse<List<CourtDTO>> options(@RequestParam(value = "venueId", required = false) Long venueId) {
        return courtService.options(venueId);
    }

    @Operation(summary = "新增片场")
    @PostMapping
    public ApiResponse<CourtDTO> create(@RequestBody CourtSaveRequest request) {
        return courtService.create(request);
    }

    @Operation(summary = "编辑片场")
    @PutMapping("/{id}")
    public ApiResponse<CourtDTO> update(@PathVariable("id") Long id, @RequestBody CourtSaveRequest request) {
        return courtService.update(id, request);
    }

    @Operation(summary = "启停片场")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return courtService.updateStatus(id, body.get("status"));
    }
}
