package com.sportvenue.venue.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实时人数推送WebSocket端点
 */
@Slf4j
@Component
@ServerEndpoint("/ws/occupancy/{venueId}")
public class OccupancyWebSocket {

    private static RedisTemplate<String, Integer> redisTemplate;
    private static final ConcurrentHashMap<String, Session> SESSIONS = new ConcurrentHashMap<>();

    @Autowired
    @Qualifier("integerRedisTemplate")
    public void setRedisTemplate(RedisTemplate<String, Integer> redisTemplate) {
        OccupancyWebSocket.redisTemplate = redisTemplate;
    }

    @OnOpen
    public void onOpen(Session session, @PathParam("venueId") Long venueId) {
        String sessionId = venueId + "_" + session.getId();
        SESSIONS.put(sessionId, session);
        log.info("WebSocket连接建立: venueId={}, sessionId={}", venueId, session.getId());
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到WebSocket消息: {}", message);
        // 处理客户端消息
    }

    @OnClose
    public void onClose(Session session) {
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().equals(session));
        log.info("WebSocket连接关闭: sessionId={}", session.getId());
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket错误: sessionId={}", session.getId(), error);
        SESSIONS.entrySet().removeIf(entry -> entry.getValue().equals(session));
    }

    /**
     * 广播人数更新
     */
    public static void broadcastOccupancy(Long venueId, int current, int predicted) {
        String msg = String.format("{\"venueId\":%d,\"current\":%d,\"predicted\":%d,\"timestamp\":%d}",
                venueId, current, predicted, System.currentTimeMillis());
        
        SESSIONS.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(venueId + "_"))
                .forEach(entry -> {
                    try {
                        entry.getValue().getBasicRemote().sendText(msg);
                    } catch (Exception e) {
                        log.error("发送WebSocket消息失败", e);
                    }
                });
    }
} 