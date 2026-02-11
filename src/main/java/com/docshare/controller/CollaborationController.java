package com.docshare.controller;

import com.docshare.dto.CursorPosition;
import com.docshare.dto.DocumentChange;
import com.docshare.dto.UserPresence;
import com.docshare.service.CollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class CollaborationController {
    
    private final CollaborationService collaborationService;
    
    @MessageMapping("/document/{documentId}/join")
    public void userJoin(@DestinationVariable Long documentId, 
                         @Payload UserPresence presence,
                         SimpMessageHeaderAccessor headerAccessor) {
        log.info("User {} joined document {}", presence.getUsername(), documentId);
        presence.setAction("JOIN");
        presence.setTimestamp(System.currentTimeMillis());
        
        // Store session info
        headerAccessor.getSessionAttributes().put("userId", presence.getUserId());
        headerAccessor.getSessionAttributes().put("documentId", documentId);
        
        collaborationService.broadcastPresence(documentId, presence);
    }
    
    @MessageMapping("/document/{documentId}/leave")
    public void userLeave(@DestinationVariable Long documentId, 
                          @Payload UserPresence presence) {
        log.info("User {} left document {}", presence.getUsername(), documentId);
        presence.setAction("LEAVE");
        presence.setTimestamp(System.currentTimeMillis());
        
        collaborationService.broadcastPresence(documentId, presence);
    }
    
    @MessageMapping("/document/{documentId}/cursor")
    public void cursorMove(@DestinationVariable Long documentId, 
                           @Payload CursorPosition cursor) {
        cursor.setTimestamp(System.currentTimeMillis());
        collaborationService.broadcastCursor(documentId, cursor);
    }
    
    @MessageMapping("/document/{documentId}/change")
    public void documentChange(@DestinationVariable Long documentId, 
                               @Payload DocumentChange change) {
        log.info("Document {} changed by user {}", documentId, change.getUsername());
        change.setTimestamp(System.currentTimeMillis());
        
        collaborationService.broadcastChange(documentId, change);
    }
}
