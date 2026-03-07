package com.docshare.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPresence {
    
    private Long userId;
    private String username;
    private String fullName;
    private String avatarUrl;
    private Long documentId;
    private String action;
    private String color;
    private Long timestamp;
}
