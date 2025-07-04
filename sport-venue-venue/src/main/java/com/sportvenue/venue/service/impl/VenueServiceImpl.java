package com.sportvenue.venue.service.impl;

import com.sportvenue.venue.service.VenueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 场馆服务实现类
 */
@Slf4j
@Service
public class VenueServiceImpl implements VenueService {

    @Override
    public String getVenueInfo(Long venueId) {
        log.info("获取场馆 {} 的信息", venueId);
        return "场馆 " + venueId + " 的信息";
    }

    @Override
    public int getCurrentOccupancy(Long venueId) {
        log.info("获取场馆 {} 的当前人数", venueId);
        return (int) (Math.random() * 100);
    }
} 