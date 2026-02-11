package com.docshare.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private NotificationType type;
    private Long recipientUserId;
    private String recipientEmail;
    private String recipientUsername;
    private String title;
    private String message;
    private Long documentId;
    private String documentTitle;
    private Long actorUserId;
    private String actorUsername;
    private LocalDateTime timestamp;
    
    public enum NotificationType {
        DOCUMENT_SHARED,
        DOCUMENT_EDITED,
        PERMISSION_GRANTED,
        PERMISSION_REVOKED,
        MENTION_IN_COMMENT
    }
}
