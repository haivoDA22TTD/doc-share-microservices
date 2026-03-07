package com.docshare.document.service;

import com.docshare.document.dto.CreateDocumentRequest;
import com.docshare.document.dto.DocumentDto;
import com.docshare.document.dto.UpdateDocumentRequest;
import com.docshare.document.entity.Document;
import com.docshare.document.repository.DocumentRepository;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentService {
    
    private final DocumentRepository documentRepository;
    private final PermissionService permissionService;
    private final CacheService cacheService;
    private final MetricsService metricsService;
    
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long) authentication.getPrincipal();
    }
    
    @Transactional
    public DocumentDto createDocument(CreateDocumentRequest request) {
        Long userId = getCurrentUserId();
        
        Document document = Document.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .ownerId(userId)
                .isPublic(request.getIsPublic() != null ? request.getIsPublic() : false)
                .build();
        
        Document saved = documentRepository.save(document);
        
        metricsService.incrementDocumentCreated();
        
        log.info("Document created: {} by user: {}", saved.getId(), userId);
        
        return convertToDto(saved, userId);
    }
    
    @Transactional(readOnly = true)
    public DocumentDto getDocument(Long id) {
        Long userId = getCurrentUserId();
        
        Timer.Sample sample = metricsService.startDocumentLoadTimer();
        
        DocumentDto cached = cacheService.getCachedDocument(id);
        if (cached != null) {
            metricsService.stopDocumentLoadTimer(sample);
            return cached;
        }
        
        Document document = documentRepository.findByIdAndUserHasAccess(id, userId)
                .orElseThrow(() -> new RuntimeException("Document not found or access denied"));
        
        DocumentDto dto = convertToDto(document, userId);
        cacheService.cacheDocument(dto);
        
        metricsService.stopDocumentLoadTimer(sample);
        
        return dto;
    }
    
    @Transactional(readOnly = true)
    public List<DocumentDto> getUserDocuments() {
        Long userId = getCurrentUserId();
        
        List<Document> ownedDocs = documentRepository.findByOwnerId(userId);
        List<Document> sharedDocs = documentRepository.findSharedWithUser(userId);
        
        List<DocumentDto> result = ownedDocs.stream()
                .map(doc -> convertToDto(doc, userId))
                .collect(Collectors.toList());
        
        result.addAll(sharedDocs.stream()
                .map(doc -> convertToDto(doc, userId))
                .collect(Collectors.toList()));
        
        return result;
    }
    
    @Transactional
    public DocumentDto updateDocument(Long id, UpdateDocumentRequest request) {
        Long userId = getCurrentUserId();
        
        Timer.Sample sample = metricsService.startDocumentSaveTimer();
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwnerId().equals(userId) && 
            !permissionService.hasEditPermission(id, userId)) {
            throw new RuntimeException("Access denied");
        }
        
        if (request.getTitle() != null) {
            document.setTitle(request.getTitle());
        }
        if (request.getContent() != null) {
            document.setContent(request.getContent());
        }
        if (request.getIsPublic() != null) {
            document.setIsPublic(request.getIsPublic());
        }
        
        document.setLastEditedAt(LocalDateTime.now());
        
        Document saved = documentRepository.save(document);
        
        cacheService.evictDocument(id);
        
        metricsService.incrementDocumentEdited();
        metricsService.stopDocumentSaveTimer(sample);
        
        log.info("Document updated: {} by user: {}", id, userId);
        
        return convertToDto(saved, userId);
    }
    
    @Transactional
    public void deleteDocument(Long id) {
        Long userId = getCurrentUserId();
        
        Document document = documentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        
        if (!document.getOwnerId().equals(userId)) {
            throw new RuntimeException("Only owner can delete document");
        }
        
        documentRepository.delete(document);
        cacheService.evictDocument(id);
        
        log.info("Document deleted: {} by user: {}", id, userId);
    }
    
    private DocumentDto convertToDto(Document document, Long currentUserId) {
        String permission = "NONE";
        
        if (document.getOwnerId().equals(currentUserId)) {
            permission = "OWNER";
        } else if (permissionService.hasEditPermission(document.getId(), currentUserId)) {
            permission = "EDIT";
        } else if (permissionService.hasViewPermission(document.getId(), currentUserId)) {
            permission = "VIEW";
        }
        
        return DocumentDto.builder()
                .id(document.getId())
                .title(document.getTitle())
                .content(document.getContent())
                .ownerId(document.getOwnerId())
                .ownerUsername("User" + document.getOwnerId())
                .isPublic(document.getIsPublic())
                .userPermission(permission)
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt())
                .lastEditedAt(document.getLastEditedAt())
                .build();
    }
}
