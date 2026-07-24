package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.TeamAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamAuditLogRepository extends JpaRepository<TeamAuditLog, Long> {

    List<TeamAuditLog> findByTeamIdAndMerchantIdOrderByCreateTimeDesc(Long teamId, Long merchantId);
}
