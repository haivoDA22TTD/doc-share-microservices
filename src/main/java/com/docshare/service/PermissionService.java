package com.docshare.service;

import com.docshare.dto.DocumentPermissionDto;
import com.docshare.dto.ShareDocumentRequest;
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

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionService {
    
    private final DocumentRepository documentRepository;
    private final DocumentPermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final EventPublisher eventPublisher;
    
    @Transactional
    public DocumentPermissionDto shareDocument(Long documentId, ShareDocumentRequest request) {
        User currentUser = getCurrentUser();
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only owner can share document");
        }
        
        User targetUser = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found: " + request.getUsername()));
        
        if (targetUser.getId().equals(currentUser.getId())) {
            throw new RuntimeException("Cannot share document with yourself");
        }
        
        DocumentPermission.PermissionType permissionType;
        try {
            permissionType = DocumentPermission.PermissionType.valueOf(request.getPermission().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid permission type. Use VIEW or EDIT");
        }
        
        DocumentPermission permission = permissionRepository.findByDocumentAndUser(document, targetUser)
                .orElse(DocumentPermission.builder()
                        .document(document)
                        .user(targetUser)
                        .build());
        
        permission.setPermission(permissionType);
        DocumentPermission savedPermission = permissionRepository.save(permission);
        
        // Publish document shared event
        eventPublisher.publishDocumentShared(
                document.getId(),
                document.getTitle(),
                currentUser.getId(),
                currentUser.getUsername(),
                targetUser.getId(),
                targetUser.getUsername(),
                permissionType.name()
        );
        
        // Publish notification event
        com.docshare.event.NotificationEvent notification = com.docshare.event.NotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .type(com.docshare.event.NotificationEvent.NotificationType.DOCUMENT_SHARED)
                .recipientUserId(targetUser.getId())
                .recipientEmail(targetUser.getEmail())
                .recipientUsername(targetUser.getUsername())
                .title("Document Shared With You")
                .message(String.format("%s shared document '%s' with you (%s permission)", 
                        currentUser.getUsername(), document.getTitle(), permissionType.name()))
                .documentId(document.getId())
                .documentTitle(document.getTitle())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getUsername())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        eventPublisher.publishNotification(notification);
        
        return convertToDto(savedPermission);
    }
    
    @Transactional
    public void revokePermission(Long documentId, Long userId) {
        User currentUser = getCurrentUser();
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only owner can revoke permissions");
        }
        
        User targetUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Publish notification before revoking
        com.docshare.event.NotificationEvent notification = com.docshare.event.NotificationEvent.builder()
                .eventId(java.util.UUID.randomUUID().toString())
                .type(com.docshare.event.NotificationEvent.NotificationType.PERMISSION_REVOKED)
                .recipientUserId(targetUser.getId())
                .recipientEmail(targetUser.getEmail())
                .recipientUsername(targetUser.getUsername())
                .title("Document Access Revoked")
                .message(String.format("%s revoked your access to document '%s'", 
                        currentUser.getUsername(), document.getTitle()))
                .documentId(document.getId())
                .documentTitle(document.getTitle())
                .actorUserId(currentUser.getId())
                .actorUsername(currentUser.getUsername())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        eventPublisher.publishNotification(notification);
        
        permissionRepository.deleteByDocumentAndUser(document, targetUser);
    }
    
    @Transactional(readOnly = true)
    public List<DocumentPermissionDto> getDocumentPermissions(Long documentId) {
        User currentUser = getCurrentUser();
        
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwner().getId().equals(currentUser.getId())) {
            throw new RuntimeException("Only owner can view permissions");
        }
        
        List<DocumentPermission> permissions = permissionRepository.findByDocumentId(documentId);
        return permissions.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        return userRepository.findById(userPrincipal.getId())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    private DocumentPermissionDto convertToDto(DocumentPermission permission) {
        return DocumentPermissionDto.builder()
                .id(permission.getId())
                .userId(permission.getUser().getId())
                .username(permission.getUser().getUsername())
                .email(permission.getUser().getEmail())
                .permission(permission.getPermission().name())
                .grantedAt(permission.getGrantedAt())
                .build();
    }
}
