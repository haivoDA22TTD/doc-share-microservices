package com.docshare.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CursorPosition {
    
    private Long userId;
    private String username;
    private Long documentId;
    private Integer index;
    private Integer length;
    private String color;
    private Long timestamp;
}
