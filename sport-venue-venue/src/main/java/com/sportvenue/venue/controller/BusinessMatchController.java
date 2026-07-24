package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.match.MatchResultDTO;
import com.sportvenue.venue.dto.match.MatchSaveRequest;
import com.sportvenue.venue.dto.match.TeamRankingDTO;
import com.sportvenue.venue.service.MatchService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "B端赛果与排行")
@RestController
@RequestMapping("/business/matches")
@CrossOrigin(origins = "*")
public class BusinessMatchController {

    @Autowired
    private MatchService matchService;

    @GetMapping
    public ApiResponse<List<MatchResultDTO>> list() {
        return matchService.list();
    }

    @GetMapping("/ranking")
    public ApiResponse<List<TeamRankingDTO>> ranking() {
        return matchService.ranking();
    }

    @PostMapping
    public ApiResponse<MatchResultDTO> create(@RequestBody MatchSaveRequest request) {
        return matchService.create(request);
    }

    @PutMapping("/{id}")
    public ApiResponse<MatchResultDTO> update(@PathVariable("id") Long id, @RequestBody MatchSaveRequest request) {
        return matchService.update(id, request);
    }
}
