package com.docshare.repository;

import com.docshare.entity.Document;
import com.docshare.entity.DocumentPermission;
import com.docshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentPermissionRepository extends JpaRepository<DocumentPermission, Long> {
    
    List<DocumentPermission> findByDocument(Document document);
    
    List<DocumentPermission> findByDocumentId(Long documentId);
    
    Optional<DocumentPermission> findByDocumentAndUser(Document document, User user);
    
    void deleteByDocumentAndUser(Document document, User user);
    
    void deleteByDocument(Document document);
}
