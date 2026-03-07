package com.docshare.document.config;

import com.docshare.document.service.MetricsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    
    private final MetricsService metricsService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection");
        metricsService.incrementActiveWebSocketConnections();
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long documentId = (Long) headerAccessor.getSessionAttributes().get("documentId");
        
        if (userId != null && documentId != null) {
            log.info("User {} disconnected from document {}", userId, documentId);
        }
        
        metricsService.decrementActiveWebSocketConnections();
    }
}
