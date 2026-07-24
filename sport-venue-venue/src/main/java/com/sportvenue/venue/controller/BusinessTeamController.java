package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.team.*;
import com.sportvenue.venue.service.TeamService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "B端球队管理")
@RestController
@RequestMapping("/business/teams")
@CrossOrigin(origins = "*")
public class BusinessTeamController {

    @Autowired
    private TeamService teamService;

    @GetMapping
    public ApiResponse<List<TeamDTO>> list() {
        return teamService.list();
    }

    @GetMapping("/options")
    public ApiResponse<List<TeamDTO>> options() {
        return teamService.options();
    }

    @GetMapping("/{id}")
    public ApiResponse<TeamDetailDTO> detail(@PathVariable("id") Long id) {
        return teamService.detail(id);
    }

    @PostMapping
    public ApiResponse<TeamDTO> create(@RequestBody TeamSaveRequest request) {
        return teamService.create(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<TeamDTO> update(@PathVariable("id") Long id, @RequestBody TeamSaveRequest request) {
        return teamService.update(id, request);
    }

    @PutMapping("/{id}/liaison")
    public ApiResponse<TeamDTO> changeLiaison(@PathVariable("id") Long id, @RequestBody TeamLiaisonRequest request) {
        return teamService.changeLiaison(id, request);
    }

    @PutMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return teamService.updateStatus(id, body.get("status"));
    }

    @GetMapping("/{id}/audits")
    public ApiResponse<List<TeamAuditDTO>> audits(@PathVariable("id") Long id) {
        return teamService.audits(id);
    }
}
