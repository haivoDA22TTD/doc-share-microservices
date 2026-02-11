package com.docshare.service;

import com.docshare.dto.CursorPosition;
import com.docshare.dto.DocumentChange;
import com.docshare.dto.UserPresence;
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
    
    // Track active users per document
    private final Map<Long, Set<UserPresence>> activeUsers = new ConcurrentHashMap<>();
    
    public void broadcastPresence(Long documentId, UserPresence presence) {
        // Update active users list
        if ("JOIN".equals(presence.getAction())) {
            Set<UserPresence> users = activeUsers.computeIfAbsent(documentId, k -> new CopyOnWriteArraySet<>());
            boolean isNewSession = users.isEmpty();
            users.add(presence);
            
            // Track collaboration session metric (only when first user joins)
            if (isNewSession) {
                metricsService.incrementCollaborationSession();
                metricsService.incrementActiveCollaborationSessions();
            }
        } else if ("LEAVE".equals(presence.getAction())) {
            Set<UserPresence> users = activeUsers.get(documentId);
            if (users != null) {
                users.removeIf(u -> u.getUserId().equals(presence.getUserId()));
                
                // Decrement active sessions when last user leaves
                if (users.isEmpty()) {
                    metricsService.decrementActiveCollaborationSessions();
                }
            }
        }
        
        // Broadcast to all users in the document
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/presence", 
                presence
        );
        
        // Send current active users list
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/users",
                activeUsers.getOrDefault(documentId, Set.of())
        );
    }
    
    public void broadcastCursor(Long documentId, CursorPosition cursor) {
        messagingTemplate.convertAndSend(
                "/topic/document/" + documentId + "/cursor",
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
