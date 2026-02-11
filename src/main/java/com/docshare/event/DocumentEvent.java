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
public class DocumentEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private EventType eventType;
    private Long documentId;
    private String documentTitle;
    private Long userId;
    private String username;
    private String userEmail;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    
    public enum EventType {
        DOCUMENT_CREATED,
        DOCUMENT_UPDATED,
        DOCUMENT_DELETED,
        DOCUMENT_SHARED,
        PERMISSION_REVOKED
    }
}
