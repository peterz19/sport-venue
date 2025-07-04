package com.sportvenue.prediction.service;

/**
 * 预测服务接口
 */
public interface PredictionService {
    
    /**
     * 预测场馆人数
     * @param venueId 场馆ID
     * @return 预测人数
     */
    int predictOccupancy(Long venueId);
} 