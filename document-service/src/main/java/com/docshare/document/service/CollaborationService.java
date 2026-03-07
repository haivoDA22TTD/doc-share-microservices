package com.docshare.document.service;

import com.docshare.document.dto.CursorPosition;
import com.docshare.document.dto.DocumentChange;
import com.docshare.document.dto.UserPresence;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Service
@RequiredArgsConstructor
@Slf4j
public class CollaborationService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final MetricsService metricsService;
    
    private final Map<Long, Set<UserPresence>> activeUsers = new ConcurrentHashMap<>();
    
    public void broadcastPresence(Long documentId, UserPresence presence) {
        if ("JOIN".equals(presence.getAction())) {
            Set<UserPresence> users = activeUsers.computeIfAbsent(documentId, k -> new CopyOnWriteArraySet<>());
            boolean isNewSession = users.isEmpty();
            users.add(presence);
            
            if (isNewSession) {
                metricsService.incrementCollaborationSession();
                metricsService.incrementActiveCollaborationSessions();
            }
        } else if ("LEAVE".equals(presence.getAction())) {
            Set<UserPresence> users = activeUsers.get(documentId);
            if (users != null) {
                users.removeIf(u -> u.getUserId().equals(presence.getUserId()));
                
                if (users.isEmpty()) {
                    metricsService.decrementActiveCollaborationSessions();
                }
            }
        }
        
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/presence", 
                presence
        );
        
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/users",
                activeUsers.getOrDefault(documentId, Set.of())
        );
    }
    
    public void broadcastCursor(Long documentId, CursorPosition cursor) {
        log.info("Broadcasting cursor for user {} at index {} in document {}", 
                cursor.getUsername(), cursor.getIndex(), documentId);
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/cursors",
                cursor
        );
    }
    
    public void broadcastChange(Long documentId, DocumentChange change) {
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/changes",
                change
        );
    }
    
    public Set<UserPresence> getActiveUsers(Long documentId) {
        return activeUsers.getOrDefault(documentId, Set.of());
    }
    
    public void removeUser(Long documentId, Long userId) {
        Set<UserPresence> users = activeUsers.get(documentId);
        if (users != null) {
            users.removeIf(u -> u.getUserId().equals(userId));
        }
    }
}
