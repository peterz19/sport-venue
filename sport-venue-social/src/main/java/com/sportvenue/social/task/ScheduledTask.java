package com.sportvenue.social.task;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 社交服务定时任务
 */
@Slf4j
@Component
public class ScheduledTask {

    @Scheduled(cron = "${social.task.cron:0 */5 * * * *}")
    public void scheduledTask() {
        log.info("执行社交服务定时任务: {}", System.currentTimeMillis());
        // 这里可以添加具体的业务逻辑
    }
} 