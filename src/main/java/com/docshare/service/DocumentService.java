package com.docshare.service;

import com.docshare.dto.CreateDocumentRequest;
import com.docshare.dto.DocumentDto;
import com.docshare.dto.UpdateDocumentRequest;
import com.docshare.entity.Document;
import com.docshare.entity.DocumentPermission;
import com.docshare.entity.User;
import com.docshare.repository.DocumentPermissionRepository;
import com.docshare.repository.DocumentRepository;
import com.docshare.repository.UserRepository;
import com.docshare.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final MetricsService metricsService;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request) {
        User currentUser = getCurrentUser();
        
        Document document = Document.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .owner(currentUser)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .lastEditedAt(LocalDateTime.now())
                .build();
        
        Document savedDocument = documentRepository.save(document);
        
        // Track document creation metric
        metricsService.incrementDocumentCreated();
        
        // Publish document created event
        eventPublisher.publishDocumentCreated(
                savedDocument.getId(),
                savedDocument.getTitle(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail()
        );
        
        return convertToDto(savedDocument, "OWNER");
    }
    
    @Transactional
    public DocumentDto updateDocument(Long documentId, UpdateDocumentRequest request) {
        User currentUser = getCurrentUser();
        Document document = getDocumentWithEditPermission(documentId, currentUser.getId());
        
        // Start timer for save operation
        var timer = metricsService.startDocumentSaveTimer();
        
        try {
            if (request.getTitle() != null) {
                document.setTitle(request.getTitle());
            }
            if (request.getContent() != null) {
                document.setContent(request.getContent());
            }
            if (request.getIsPublic() != null && document.getOwner().getId().equals(currentUser.getId())) {
                document.setIsPublic(request.getIsPublic());
            }
            
            document.setLastEditedAt(LocalDateTime.now());
            Document updatedDocument = documentRepository.save(document);
            
            // Track document edit metric
            metricsService.incrementDocumentEdited();
            
            // Publish document updated event
            eventPublisher.publishDocumentUpdated(
                    updatedDocument.getId(),
                    updatedDocument.getTitle(),
                    currentUser.getId(),
                    currentUser.getUsername(),
                    currentUser.getEmail()
            );
            
            String permission = getPermissionType(document, currentUser);
            return convertToDto(updatedDocument, permission);
        } finally {
            // Stop timer
            metricsService.stopDocumentSaveTimer(timer);
        }
    }
    
    @Transactional
    public void deleteDocument(Long documentId) {
        User currentUser = getCurrentUser();
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only owner can delete document");
        }
        
        // Publish document deleted event before deletion
        eventPublisher.publishDocumentDeleted(
                document.getId(),
                document.getTitle(),
                currentUser.getId(),
                currentUser.getUsername(),
                currentUser.getEmail()
        );
        
        permissionRepository.deleteByDocument(document);
        documentRepository.delete(document);
    }
    
    @Transactional(readOnly = true)
    public DocumentDto getDocument(Long documentId) {
        User currentUser = getCurrentUser();
        
        // Start timer for load operation
        var timer = metricsService.startDocumentLoadTimer();
        
        try {
            Document document = documentRepository.findByIdAndUserHasAccess(documentId, currentUser.getId())
                    .orElseThrow(() -> new RuntimeException("Document not found or access denied"));
            
            String permission = getPermissionType(document, currentUser);
            return convertToDto(document, permission);
        } finally {
            // Stop timer
            metricsService.stopDocumentLoadTimer(timer);
        }
    }
    
    @Transactional(readOnly = true)
    public List<DocumentDto> getMyDocuments() {
        User currentUser = getCurrentUser();
        List<Document> documents = documentRepository.findByOwnerId(currentUser.getId());
        return documents.stream()
                .map(doc -> convertToDto(doc, "OWNER"))
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<DocumentDto> getSharedDocuments() {
        User currentUser = getCurrentUser();
        List<Document> documents = documentRepository.findSharedWithUser(currentUser.getId());
        return documents.stream()
                .map(doc -> {
                    String permission = getPermissionType(doc, currentUser);
                    return convertToDto(doc, permission);
                })
                .collect(Collectors.toList());
    }
    
    private Document getDocumentWithEditPermission(Long documentId, Long userId) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (document.getOwner().getId().equals(userId)) {
            return document;
        }
        
        DocumentPermission permission = permissionRepository.findByDocumentAndUser(
                document, 
                userRepository.findById(userId).orElseThrow()
        ).orElseThrow(() -> new RuntimeException("Access denied"));
        
        if (permission.getPermission() != DocumentPermission.PermissionType.EDIT) {
            throw new RuntimeException("Edit permission required");
        }
        
        return document;
    }
    
    private String getPermissionType(Document document, User user) {
        if (document.getOwner().getId().equals(user.getId())) {
            return "OWNER";
        }
        
        return permissionRepository.findByDocumentAndUser(document, user)
                .map(p -> p.getPermission().name())
                .orElse("VIEW");
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private DocumentDto convertToDto(Document document, String permission) {
        return DocumentDto.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .ownerId(document.getOwner().getId())
                .ownerUsername(document.getOwner().getUsername())
                .isPublic(document.getIsPublic())
                .userPermission(permission)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .lastEditedAt(document.getLastEditedAt())
                .build();
    }
}
