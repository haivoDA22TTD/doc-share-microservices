package com.docshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentDto implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String title;
    private String content;
    private Long ownerId;
    private String ownerUsername;
    private Boolean isPublic;
    private String userPermission;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime lastEditedAt;
}
