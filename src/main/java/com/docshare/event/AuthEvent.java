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
public class AuthEvent implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String eventId;
    private EventType eventType;
    private Long userId;
    private String username;
    private String email;
    private LocalDateTime timestamp;
    private String ipAddress;
    private String userAgent;
    private boolean success;
    private String failureReason;
    
    public enum EventType {
        USER_REGISTERED,
        USER_LOGIN,
        USER_LOGOUT,
        LOGIN_FAILED,
        TOKEN_BLACKLISTED
    }
}
