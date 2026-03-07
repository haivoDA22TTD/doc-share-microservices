package com.docshare.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentPermissionDto {
    
    private Long id;
    private Long documentId;
    private Long userId;
    private String username;
    private String permission;
    private LocalDateTime grantedAt;
}
