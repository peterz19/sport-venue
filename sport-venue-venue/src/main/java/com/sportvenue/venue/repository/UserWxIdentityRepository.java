package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.UserWxIdentity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserWxIdentityRepository extends JpaRepository<UserWxIdentity, Long> {
    Optional<UserWxIdentity> findByAppIdAndOpenid(String appId, String openid);
}
