package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.CustomerUser;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerUserRepository extends JpaRepository<CustomerUser, Long> {
}
