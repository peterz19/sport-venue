package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.dto.staff.StaffCreateRequest;
import com.sportvenue.venue.dto.staff.StaffDTO;
import com.sportvenue.venue.dto.staff.StaffUpdateRequest;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.UserRepository;
import com.sportvenue.venue.service.StaffService;
import com.sportvenue.venue.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class StaffServiceImpl implements StaffService {

    private static final List<User.UserType> STAFF_TYPES =
            Arrays.asList(User.UserType.B_MERCHANT, User.UserType.B_STAFF);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<List<StaffDTO>> listStaff() {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            List<StaffDTO> list = userRepository.findByMerchantIdAndUserTypeIn(merchantId, STAFF_TYPES).stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询员工列表失败", e);
            return ApiResponse.error("查询员工列表失败");
        }
    }

    @Override
    public ApiResponse<List<StaffDTO>> listOptions() {
        try {
            Long merchantId = SecurityUtils.requireMerchantId();
            List<StaffDTO> list = userRepository
                    .findByMerchantIdAndUserTypeInAndStatus(merchantId, STAFF_TYPES, User.UserStatus.ACTIVE)
                    .stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return ApiResponse.success(list);
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("查询员工选项失败", e);
            return ApiResponse.error("查询员工选项失败");
        }
    }

    @Override
    public ApiResponse<StaffDTO> createStaff(StaffCreateRequest request) {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            User owner = SecurityUtils.requireCurrentUser();

            if (!StringUtils.hasText(request.getUsername()) || !StringUtils.hasText(request.getPassword())) {
                throw new BusinessException("用户名和密码不能为空");
            }
            if (request.getPassword().length() < 6) {
                throw new BusinessException("密码至少6位");
            }
            if (!StringUtils.hasText(request.getRealName())) {
                throw new BusinessException("姓名不能为空");
            }
            if (userRepository.existsByUsername(request.getUsername().trim())) {
                throw new BusinessException("用户名已存在");
            }
            if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone().trim())) {
                throw new BusinessException("手机号已存在");
            }

            User staff = new User();
            staff.setUsername(request.getUsername().trim());
            staff.setPassword(passwordEncoder.encode(request.getPassword()));
            staff.setRealName(request.getRealName().trim());
            staff.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
            staff.setUserType(User.UserType.B_STAFF);
            staff.setMerchantId(merchantId);
            staff.setMerchantName(owner.getMerchantName());
            staff.setStatus(User.UserStatus.ACTIVE);
            staff.setRemark(request.getRemark());
            staff.setPoints(0);
            staff.setCreateTime(LocalDateTime.now());
            staff.setUpdateTime(LocalDateTime.now());
            staff.setCreateBy(owner.getId());

            User saved = userRepository.save(staff);
            return ApiResponse.success(toDto(saved));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("创建员工失败", e);
            return ApiResponse.error("创建员工失败");
        }
    }

    @Override
    public ApiResponse<StaffDTO> updateStaff(Long id, StaffUpdateRequest request) {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            User staff = requireMerchantStaff(id, merchantId);

            if (StringUtils.hasText(request.getRealName())) {
                staff.setRealName(request.getRealName().trim());
            }
            if (request.getPhone() != null) {
                String phone = request.getPhone().trim();
                if (StringUtils.hasText(phone)
                        && (staff.getPhone() == null || !phone.equals(staff.getPhone()))
                        && userRepository.existsByPhone(phone)) {
                    throw new BusinessException("手机号已存在");
                }
                staff.setPhone(StringUtils.hasText(phone) ? phone : null);
            }
            if (request.getRemark() != null) {
                staff.setRemark(request.getRemark());
            }
            staff.setUpdateTime(LocalDateTime.now());
            staff.setUpdateBy(SecurityUtils.currentUserId());
            return ApiResponse.success(toDto(userRepository.save(staff)));
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新员工失败", e);
            return ApiResponse.error("更新员工失败");
        }
    }

    @Override
    public ApiResponse<Void> updateStatus(Long id, String status) {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            User staff = requireMerchantStaff(id, merchantId);
            if (staff.getId().equals(SecurityUtils.currentUserId())) {
                throw new BusinessException("不能停用自己的账号");
            }
            if (staff.getUserType() == User.UserType.B_MERCHANT) {
                throw new BusinessException("不能通过此接口停用老板账号");
            }
            User.UserStatus statusEnum = User.UserStatus.valueOf(status.toUpperCase());
            if (statusEnum != User.UserStatus.ACTIVE && statusEnum != User.UserStatus.INACTIVE) {
                throw new BusinessException("仅支持 ACTIVE / INACTIVE");
            }
            staff.setStatus(statusEnum);
            staff.setUpdateTime(LocalDateTime.now());
            staff.setUpdateBy(SecurityUtils.currentUserId());
            userRepository.save(staff);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("更新员工状态失败", e);
            return ApiResponse.error("更新员工状态失败");
        }
    }

    @Override
    public ApiResponse<Void> resetPassword(Long id, Map<String, String> body) {
        try {
            SecurityUtils.requireOwner();
            Long merchantId = SecurityUtils.requireMerchantId();
            User staff = requireMerchantStaff(id, merchantId);
            String password = body == null ? null : body.get("password");
            if (!StringUtils.hasText(password) || password.length() < 6) {
                throw new BusinessException("新密码至少6位");
            }
            staff.setPassword(passwordEncoder.encode(password));
            staff.setUpdateTime(LocalDateTime.now());
            staff.setUpdateBy(SecurityUtils.currentUserId());
            userRepository.save(staff);
            return ApiResponse.success();
        } catch (BusinessException e) {
            return ApiResponse.error(e.getCode(), e.getMessage());
        } catch (Exception e) {
            log.error("重置员工密码失败", e);
            return ApiResponse.error("重置员工密码失败");
        }
    }

    private User requireMerchantStaff(Long id, Long merchantId) {
        User staff = userRepository.findByIdAndMerchantId(id, merchantId)
                .orElseThrow(() -> new BusinessException("员工不存在"));
        if (staff.getUserType() != User.UserType.B_MERCHANT
                && staff.getUserType() != User.UserType.B_STAFF) {
            throw new BusinessException("非商户员工账号");
        }
        return staff;
    }

    private StaffDTO toDto(User user) {
        String role = user.getUserType() == User.UserType.B_MERCHANT ? "OWNER" : "STAFF";
        return StaffDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .userType(user.getUserType().name())
                .role(role)
                .status(user.getStatus().name())
                .remark(user.getRemark())
                .merchantId(user.getMerchantId())
                .merchantName(user.getMerchantName())
                .build();
    }
}
