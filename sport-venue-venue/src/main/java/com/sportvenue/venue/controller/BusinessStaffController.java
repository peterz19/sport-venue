package com.sportvenue.venue.controller;

import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.StaffCreateRequest;
import com.sportvenue.venue.dto.staff.StaffDTO;
import com.sportvenue.venue.dto.staff.StaffUpdateRequest;
import com.sportvenue.venue.service.StaffService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "B端员工管理", description = "商户员工账号管理")
@Slf4j
@RestController
@RequestMapping("/business/staff")
@CrossOrigin(origins = "*")
public class BusinessStaffController {

    @Autowired
    private StaffService staffService;

    @Operation(summary = "员工列表（老板）")
    @GetMapping
    public ApiResponse<List<StaffDTO>> list() {
        return staffService.listStaff();
    }

    @Operation(summary = "在职员工下拉")
    @GetMapping("/options")
    public ApiResponse<List<StaffDTO>> options() {
        return staffService.listOptions();
    }

    @Operation(summary = "新增店员")
    @PostMapping
    public ApiResponse<StaffDTO> create(@RequestBody StaffCreateRequest request) {
        return staffService.createStaff(request);
    }

    @Operation(summary = "编辑员工资料")
    @PutMapping("/{id}")
    public ApiResponse<StaffDTO> update(@PathVariable("id") Long id, @RequestBody StaffUpdateRequest request) {
        return staffService.updateStaff(id, request);
    }

    @Operation(summary = "启用/停用")
    @PutMapping("/{id}/status")
    public ApiResponse<Void> status(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return staffService.updateStatus(id, body.get("status"));
    }

    @Operation(summary = "重置密码")
    @PutMapping("/{id}/password")
    public ApiResponse<Void> password(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        return staffService.resetPassword(id, body);
    }
}
