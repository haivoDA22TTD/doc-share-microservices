package com.docshare.controller;

import com.docshare.dto.*;
import com.docshare.service.DocumentService;
import com.docshare.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {
    
    private final DocumentService documentService;
    private final PermissionService permissionService;
    
    @PostMapping
    public ResponseEntity<DocumentDto> createDocument(@Valid @RequestBody CreateDocumentRequest request) {
        DocumentDto document = documentService.createDocument(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(document);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<DocumentDto> getDocument(@PathVariable Long id) {
        DocumentDto document = documentService.getDocument(id);
        return ResponseEntity.ok(document);
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<DocumentDto> updateDocument(
            @PathVariable Long id,
            @RequestBody UpdateDocumentRequest request) {
        DocumentDto document = documentService.updateDocument(id, request);
        return ResponseEntity.ok(document);
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my-documents")
    public ResponseEntity<List<DocumentDto>> getMyDocuments() {
        List<DocumentDto> documents = documentService.getMyDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @GetMapping("/shared-with-me")
    public ResponseEntity<List<DocumentDto>> getSharedDocuments() {
        List<DocumentDto> documents = documentService.getSharedDocuments();
        return ResponseEntity.ok(documents);
    }
    
    @PostMapping("/{id}/share")
    public ResponseEntity<DocumentPermissionDto> shareDocument(
            @PathVariable Long id,
            @Valid @RequestBody ShareDocumentRequest request) {
        DocumentPermissionDto permission = permissionService.shareDocument(id, request);
        return ResponseEntity.ok(permission);
    }
    
    @DeleteMapping("/{documentId}/permissions/{userId}")
    public ResponseEntity<Void> revokePermission(
            @PathVariable Long documentId,
            @PathVariable Long userId) {
        permissionService.revokePermission(documentId, userId);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<DocumentPermissionDto>> getDocumentPermissions(@PathVariable Long id) {
        List<DocumentPermissionDto> permissions = permissionService.getDocumentPermissions(id);
        return ResponseEntity.ok(permissions);
    }
}
