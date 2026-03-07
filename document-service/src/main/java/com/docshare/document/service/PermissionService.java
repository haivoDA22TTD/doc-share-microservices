package com.docshare.document.service;

import com.docshare.document.dto.DocumentPermissionDto;
import com.docshare.document.dto.ShareDocumentRequest;
import com.docshare.document.entity.DocumentPermission;
import com.docshare.document.repository.DocumentPermissionRepository;
import com.docshare.document.repository.DocumentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionService {
    
    private final DocumentPermissionRepository permissionRepository;
    private final DocumentRepository documentRepository;
    
    @Transactional
    public void shareDocument(Long documentId, Long ownerId, ShareDocumentRequest request, Long targetUserId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only owner can share document");
        }
        
        DocumentPermission.PermissionType permissionType;
        try {
            permissionType = DocumentPermission.PermissionType.valueOf(request.getPermission().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid permission type");
        }
        
        var existingPermission = permissionRepository.findByDocumentIdAndUserId(documentId, targetUserId);
        
        if (existingPermission.isPresent()) {
            var permission = existingPermission.get();
            permission.setPermission(permissionType);
            permissionRepository.save(permission);
            log.info("Updated permission for user {} on document {}", targetUserId, documentId);
        } else {
            DocumentPermission permission = DocumentPermission.builder()
                    .documentId(documentId)
                    .userId(targetUserId)
                    .permission(permissionType)
                    .build();
            permissionRepository.save(permission);
            log.info("Granted {} permission to user {} on document {}", permissionType, targetUserId, documentId);
        }
    }
    
    @Transactional(readOnly = true)
    public List<DocumentPermissionDto> getDocumentPermissions(Long documentId) {
        return permissionRepository.findByDocumentId(documentId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public boolean hasViewPermission(Long documentId, Long userId) {
        return permissionRepository.findByDocumentIdAndUserId(documentId, userId)
                .map(p -> p.getPermission() == DocumentPermission.PermissionType.VIEW ||
                         p.getPermission() == DocumentPermission.PermissionType.EDIT)
                .orElse(false);
    }
    
    @Transactional(readOnly = true)
    public boolean hasEditPermission(Long documentId, Long userId) {
        return permissionRepository.findByDocumentIdAndUserId(documentId, userId)
                .map(p -> p.getPermission() == DocumentPermission.PermissionType.EDIT)
                .orElse(false);
    }
    
    @Transactional
    public void revokePermission(Long documentId, Long ownerId, Long targetUserId) {
        var document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwnerId().equals(ownerId)) {
            throw new RuntimeException("Only owner can revoke permissions");
        }
        
        permissionRepository.deleteByDocumentIdAndUserId(documentId, targetUserId);
        log.info("Revoked permission for user {} on document {}", targetUserId, documentId);
    }
    
    private DocumentPermissionDto convertToDto(DocumentPermission permission) {
        return DocumentPermissionDto.builder()
                .id(permission.getId())
                .documentId(permission.getDocumentId())
                .userId(permission.getUserId())
                .username("User" + permission.getUserId())
                .permission(permission.getPermission().name())
                .grantedAt(permission.getGrantedAt())
                .build();
    }
}
