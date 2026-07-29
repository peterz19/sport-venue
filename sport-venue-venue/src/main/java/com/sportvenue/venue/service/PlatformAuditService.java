package com.sportvenue.venue.service;

import com.sportvenue.venue.entity.PlatformAuditLog;
import com.sportvenue.venue.entity.User;
import com.sportvenue.venue.repository.PlatformAuditLogRepository;
import com.sportvenue.venue.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlatformAuditService {

    @Autowired
    private PlatformAuditLogRepository auditLogRepository;

    @Transactional
    public void record(String action, String targetType, Long targetId, Long merchantId,
                       String beforeJson, String afterJson, String remark) {
        PlatformAuditLog log = new PlatformAuditLog();
        log.setAction(action);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setMerchantId(merchantId);
        log.setBeforeJson(beforeJson);
        log.setAfterJson(afterJson);
        log.setRemark(remark);
        try {
            User op = SecurityUtils.requireCurrentUser();
            log.setOperatorId(op.getId());
            log.setOperatorName(op.getRealName() != null ? op.getRealName() : op.getUsername());
        } catch (Exception ignored) {
            // 系统任务无操作人
        }
        auditLogRepository.save(log);
    }
}
