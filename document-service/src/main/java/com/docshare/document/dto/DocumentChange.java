package com.docshare.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentChange {
    
    private Long userId;
    private String username;
    private Long documentId;
    private String delta;
    private String content;
    private String changeType;
    private Long timestamp;
}
