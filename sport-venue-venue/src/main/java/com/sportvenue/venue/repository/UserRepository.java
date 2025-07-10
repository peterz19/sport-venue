package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户Repository接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据手机号查找用户
     */
    Optional<User> findByPhone(String phone);

    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据商户ID查找用户列表
     */
    List<User> findByMerchantId(Long merchantId);

    /**
     * 根据用户状态查找用户列表
     */
    List<User> findByStatus(User.UserStatus status);

    /**
     * 根据用户类型查找用户列表
     */
    List<User> findByUserType(User.UserType userType);

    /**
     * 根据用户类型和状态查找用户列表
     */
    List<User> findByUserTypeAndStatus(User.UserType userType, User.UserStatus status);

    /**
     * 统计指定状态的用户数量
     */
    long countByStatus(User.UserStatus status);

    /**
     * 统计指定类型的用户数量
     */
    long countByUserType(User.UserType userType);

    /**
     * 根据用户名和密码查找用户
     */
    Optional<User> findByUsernameAndPassword(String username, String password);

    /**
     * 根据手机号和密码查找用户
     */
    Optional<User> findByPhoneAndPassword(String phone, String password);

    /**
     * 根据邮箱和密码查找用户
     */
    Optional<User> findByEmailAndPassword(String email, String password);

    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查手机号是否存在
     */
    boolean existsByPhone(String phone);

    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
} 