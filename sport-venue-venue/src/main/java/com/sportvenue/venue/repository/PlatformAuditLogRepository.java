package com.sportvenue.venue.repository;

import com.sportvenue.venue.entity.PlatformAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlatformAuditLogRepository extends JpaRepository<PlatformAuditLog, Long> {
    List<PlatformAuditLog> findTop50ByMerchantIdOrderByCreateTimeDesc(Long merchantId);
}
