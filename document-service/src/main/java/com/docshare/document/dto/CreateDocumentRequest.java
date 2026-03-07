package com.docshare.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateDocumentRequest {
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String content;
    
    private Boolean isPublic = false;
}
