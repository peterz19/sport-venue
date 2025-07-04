package com.sportvenue.prediction.service.impl;

import com.sportvenue.prediction.service.PredictionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 预测服务实现类
 */
@Slf4j
@Service
public class PredictionServiceImpl implements PredictionService {

    @Override
    public int predictOccupancy(Long venueId) {
        log.info("预测场馆 {} 的人数", venueId);
        // 这里可以添加具体的预测逻辑
        return (int) (Math.random() * 100);
    }
} 