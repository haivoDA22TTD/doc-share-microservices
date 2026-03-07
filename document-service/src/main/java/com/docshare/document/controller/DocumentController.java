package com.docshare.document.controller;

import com.docshare.document.dto.CreateDocumentRequest;
import com.docshare.document.dto.DocumentDto;
import com.docshare.document.dto.DocumentPermissionDto;
import com.docshare.document.dto.ShareDocumentRequest;
import com.docshare.document.dto.UpdateDocumentRequest;
import com.docshare.document.service.DocumentService;
import com.docshare.document.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DocumentController {
    
    private final DocumentService documentService;
    private final PermissionService permissionService;
    
    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentDto document = documentService.createDocument(request);
        return ResponseEntity.ok(document);
    }
    
    @GetMapping
    public ResponseEntity<List<DocumentDto>> getUserDocuments() {
        List<DocumentDto> documents = documentService.getUserDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        DocumentDto document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(@PathVariable Long id, 
                                                      @RequestBody UpdateDocumentRequest request) {
        DocumentDto document = documentService.updateDocument(id, request);
        return ResponseEntity.ok(document);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    
    @PostMapping("/{id}/share")
    public ResponseEntity<String> shareDocument(@PathVariable Long id,
                                                @Valid @RequestBody ShareDocumentRequest request,
                                                Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        
        // For now, use a dummy target user ID
        // In production, you would call Auth Service to get user ID by username
        Long targetUserId = 2L; // Dummy value
        
        permissionService.shareDocument(id, userId, request, targetUserId);
        return ResponseEntity.ok("Document shared successfully");
    }
    
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<DocumentPermissionDto>> getDocumentPermissions(@PathVariable Long id) {
        List<DocumentPermissionDto> permissions = permissionService.getDocumentPermissions(id);
        return ResponseEntity.ok(permissions);
    }
    
    @DeleteMapping("/{id}/permissions/{userId}")
    public ResponseEntity<Void> revokePermission(@PathVariable Long id,
                                                 @PathVariable Long userId,
                                                 Authentication authentication) {
        Long ownerId = (Long) authentication.getPrincipal();
        permissionService.revokePermission(id, ownerId, userId);
        return ResponseEntity.noContent().build();
    }
}
