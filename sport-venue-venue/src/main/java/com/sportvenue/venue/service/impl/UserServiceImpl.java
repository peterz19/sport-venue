package com.sportvenue.venue.service.impl;

import com.sportvenue.common.exception.BusinessException;
import com.sportvenue.common.model.ApiResponse;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.UserRepository;
import com.sportvenue.venue.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public ApiResponse<User> createUser(User user) {
        try {
            // 检查用户名是否已存在
            if (userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new BusinessException("用户名已存在");
            }

            // 检查手机号是否已存在
            if (StringUtils.hasText(user.getPhone()) && 
                userRepository.findByPhone(user.getPhone()).isPresent()) {
                throw new BusinessException("手机号已存在");
            }

            // 检查邮箱是否已存在
            if (StringUtils.hasText(user.getEmail()) && 
                userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new BusinessException("邮箱已存在");
            }

            // 加密密码
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            user.setCreateTime(LocalDateTime.now());
            user.setUpdateTime(LocalDateTime.now());

            User savedUser = userRepository.save(user);
            log.info("用户创建成功，ID：{}", savedUser.getId());
            return ApiResponse.success(savedUser);
        } catch (Exception e) {
            log.error("创建用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("创建用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<User> updateUser(Long id, User user) {
        try {
            User existingUser = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查用户名是否被其他用户使用
            if (!existingUser.getUsername().equals(user.getUsername()) &&
                userRepository.findByUsername(user.getUsername()).isPresent()) {
                throw new BusinessException("用户名已存在");
            }

            // 检查手机号是否被其他用户使用
            if (StringUtils.hasText(user.getPhone()) && 
                !user.getPhone().equals(existingUser.getPhone()) &&
                userRepository.findByPhone(user.getPhone()).isPresent()) {
                throw new BusinessException("手机号已存在");
            }

            // 检查邮箱是否被其他用户使用
            if (StringUtils.hasText(user.getEmail()) && 
                !user.getEmail().equals(existingUser.getEmail()) &&
                userRepository.findByEmail(user.getEmail()).isPresent()) {
                throw new BusinessException("邮箱已存在");
            }

            // 更新用户信息
            existingUser.setRealName(user.getRealName());
            existingUser.setPhone(user.getPhone());
            existingUser.setEmail(user.getEmail());
            existingUser.setUserType(user.getUserType());
            existingUser.setMerchantId(user.getMerchantId());
            existingUser.setMerchantName(user.getMerchantName());
            existingUser.setStatus(user.getStatus());
            existingUser.setAvatar(user.getAvatar());
            existingUser.setPoints(user.getPoints());
            existingUser.setMemberLevel(user.getMemberLevel());
            existingUser.setRemark(user.getRemark());
            existingUser.setUpdateTime(LocalDateTime.now());

            User updatedUser = userRepository.save(existingUser);
            log.info("用户更新成功，ID：{}", updatedUser.getId());
            return ApiResponse.success(updatedUser);
        } catch (Exception e) {
            log.error("更新用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("更新用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> deleteUser(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            userRepository.delete(user);
            log.info("用户删除成功，ID：{}", id);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("删除用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<User> getUserById(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));
            return ApiResponse.success(user);
        } catch (Exception e) {
            log.error("获取用户详情失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户详情失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Page<User>> getUserList(Integer page, Integer size, String username, String userType, String status) {
        try {
            Pageable pageable = PageRequest.of(page, size);
            
            Specification<User> spec = (root, query, criteriaBuilder) -> {
                List<Predicate> predicates = new ArrayList<>();
                
                if (StringUtils.hasText(username)) {
                    predicates.add(criteriaBuilder.like(root.get("username"), "%" + username + "%"));
                }
                
                if (StringUtils.hasText(userType)) {
                    predicates.add(criteriaBuilder.equal(root.get("userType"), User.UserType.valueOf(userType.toUpperCase())));
                }
                
                if (StringUtils.hasText(status)) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), User.UserStatus.valueOf(status.toUpperCase())));
                }
                
                return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
            };
            
            Page<User> userPage = userRepository.findAll(spec, pageable);
            return ApiResponse.success(userPage);
        } catch (Exception e) {
            log.error("查询用户列表失败：{}", e.getMessage(), e);
            return ApiResponse.error("查询用户列表失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<User>> getUsersByMerchant(Long merchantId) {
        try {
            List<User> users = userRepository.findByMerchantId(merchantId);
            return ApiResponse.success(users);
        } catch (Exception e) {
            log.error("查询商户用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("查询商户用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> updateUserStatus(Long id, User.UserStatus status) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            user.setStatus(status);
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("用户状态更新成功，ID：{}，状态：{}", id, status);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("更新用户状态失败：{}", e.getMessage(), e);
            return ApiResponse.error("更新用户状态失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> resetUserPassword(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 重置为默认密码
            user.setPassword(passwordEncoder.encode("123456"));
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("用户密码重置成功，ID：{}", id);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("重置用户密码失败：{}", e.getMessage(), e);
            return ApiResponse.error("重置用户密码失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> changeUserPassword(Long id, String oldPassword, String newPassword) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // 验证旧密码
            if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
                throw new BusinessException("旧密码错误");
            }

            // 更新新密码
            user.setPassword(passwordEncoder.encode(newPassword));
            user.setUpdateTime(LocalDateTime.now());
            userRepository.save(user);
            
            log.info("用户密码修改成功，ID：{}", id);
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("修改用户密码失败：{}", e.getMessage(), e);
            return ApiResponse.error("修改用户密码失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> assignUserRoles(Long id, List<Long> roleIds) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // TODO: 实现角色分配逻辑
            log.info("用户角色分配成功，用户ID：{}，角色数量：{}", id, roleIds.size());
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("分配用户角色失败：{}", e.getMessage(), e);
            return ApiResponse.error("分配用户角色失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getUserRoles(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // TODO: 实现获取用户角色逻辑
            List<Map<String, Object>> roles = new ArrayList<>();
            return ApiResponse.success(roles);
        } catch (Exception e) {
            log.error("获取用户角色失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户角色失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<Map<String, Object>>> getUserPermissions(Long id) {
        try {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("用户不存在"));

            // TODO: 实现获取用户权限逻辑
            List<Map<String, Object>> permissions = new ArrayList<>();
            return ApiResponse.success(permissions);
        } catch (Exception e) {
            log.error("获取用户权限失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户权限失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> login(String username, String password) {
        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new BusinessException("用户名或密码错误"));

            log.info("=== 密码验证调试信息 ===");
            log.info("输入密码: {}", password);
            log.info("数据库密码: {}", user.getPassword());
            log.info("密码长度: {}", password.length());
            log.info("数据库密码长度: {}", user.getPassword().length());
            
            // 测试密码编码器
            String testEncoded = passwordEncoder.encode("123456");
            log.info("测试编码123456: {}", testEncoded);
            boolean testMatch = passwordEncoder.matches("123456", testEncoded);
            log.info("测试匹配结果: {}", testMatch);
            
            // 实际匹配
            boolean actualMatch = passwordEncoder.matches(password, user.getPassword());
            log.info("实际匹配结果: {}", actualMatch);
            log.info("=== 调试信息结束 ===");
            
            if (!actualMatch) {
                throw new BusinessException("用户名或密码错误");
            }

            if (user.getStatus() != User.UserStatus.ACTIVE) {
                throw new BusinessException("用户已被停用或锁定");
            }

            // 更新最后登录信息
            user.setLastLoginTime(LocalDateTime.now());
            userRepository.save(user);
            
            Map<String, Object> result = new HashMap<>();
            result.put("user", user);
            
            log.info("用户登录成功，用户名：{}", username);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("用户登录失败：{}", e.getMessage(), e);
            return ApiResponse.error("登录失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> logout(String token) {
        try {
            // TODO: 实现登出逻辑，清除token
            log.info("用户登出成功");
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("用户登出失败：{}", e.getMessage(), e);
            return ApiResponse.error("登出失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<User> getCurrentUser(String token) {
        try {
            // 这个方法现在由AuthController处理，这里保留接口兼容性
            return ApiResponse.error("请使用 /auth/user/info 接口获取当前用户信息");
        } catch (Exception e) {
            log.error("获取当前用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取当前用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.countByStatus(User.UserStatus.ACTIVE);
            long inactiveUsers = userRepository.countByStatus(User.UserStatus.INACTIVE);
            long lockedUsers = userRepository.countByStatus(User.UserStatus.LOCKED);

            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("activeUsers", activeUsers);
            statistics.put("inactiveUsers", inactiveUsers);
            statistics.put("lockedUsers", lockedUsers);

            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取用户统计信息失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户统计信息失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Map<String, Long>> getUserTypeStatistics() {
        try {
            Map<String, Long> statistics = new HashMap<>();
            for (User.UserType userType : User.UserType.values()) {
                long count = userRepository.countByUserType(userType);
                statistics.put(userType.name(), count);
            }
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取用户类型统计失败：{}", e.getMessage(), e);
            return ApiResponse.error("获取用户类型统计失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<Void> importUsers(List<User> users) {
        try {
            for (User user : users) {
                user.setPassword(passwordEncoder.encode(user.getPassword()));
                user.setCreateTime(LocalDateTime.now());
                user.setUpdateTime(LocalDateTime.now());
            }
            userRepository.saveAll(users);
            
            log.info("批量导入用户成功，数量：{}", users.size());
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("批量导入用户失败：{}", e.getMessage(), e);
            return ApiResponse.error("批量导入用户失败：" + e.getMessage());
        }
    }

    @Override
    public ApiResponse<List<User>> exportUsers(String userType, String status) {
        try {
            List<User> users = new ArrayList<>();
            
            if (StringUtils.hasText(userType) && StringUtils.hasText(status)) {
                users = userRepository.findByUserTypeAndStatus(
                    User.UserType.valueOf(userType.toUpperCase()),
                    User.UserStatus.valueOf(status.toUpperCase())
                );
            } else if (StringUtils.hasText(userType)) {
                users = userRepository.findByUserType(User.UserType.valueOf(userType.toUpperCase()));
            } else if (StringUtils.hasText(status)) {
                users = userRepository.findByStatus(User.UserStatus.valueOf(status.toUpperCase()));
            } else {
                users = userRepository.findAll();
            }
            
            return ApiResponse.success(users);
        } catch (Exception e) {
            log.error("导出用户数据失败：{}", e.getMessage(), e);
            return ApiResponse.error("导出用户数据失败：" + e.getMessage());
        }
    }

    /**
     * 生成用户token
     */
    private String generateToken(User user) {
        // TODO: 实现JWT token生成逻辑
        return UUID.randomUUID().toString();
    }
} 