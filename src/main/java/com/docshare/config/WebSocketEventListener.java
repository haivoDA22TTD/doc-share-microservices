package com.docshare.config;

import com.docshare.dto.UserPresence;
import com.docshare.service.CollaborationService;
import com.docshare.service.MetricsService;
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
    
    private final CollaborationService collaborationService;
    private final MetricsService metricsService;
    
    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        log.info("New WebSocket connection established");
        // Track WebSocket connection metric
        metricsService.incrementActiveWebSocketConnections();
    }
    
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        Long documentId = (Long) headerAccessor.getSessionAttributes().get("documentId");
        
        if (userId != null && documentId != null) {
            log.info("User {} disconnected from document {}", userId, documentId);
            
            // Notify others that user left
            UserPresence presence = UserPresence.builder()
                    .userId(userId)
                    .documentId(documentId)
                    .action("LEAVE")
                    .timestamp(System.currentTimeMillis())
                    .build();
            
            collaborationService.broadcastPresence(documentId, presence);
            collaborationService.removeUser(documentId, userId);
            
            // Track WebSocket disconnection metric
            metricsService.decrementActiveWebSocketConnections();
        }
    }
}
