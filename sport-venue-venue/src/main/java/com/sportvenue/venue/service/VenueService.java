package com.sportvenue.venue.service;

/**
 * 场馆服务接口
 */
public interface VenueService {
    
    /**
     * 获取场馆信息
     * @param venueId 场馆ID
     * @return 场馆信息
     */
    String getVenueInfo(Long venueId);
    
    /**
     * 获取场馆当前人数
     * @param venueId 场馆ID
     * @return 当前人数
     */
    int getCurrentOccupancy(Long venueId);
} 