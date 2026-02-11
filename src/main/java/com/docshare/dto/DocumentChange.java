package com.docshare.dto;

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
    private String delta; // Quill Delta format (JSON)
    private Long timestamp;
}
