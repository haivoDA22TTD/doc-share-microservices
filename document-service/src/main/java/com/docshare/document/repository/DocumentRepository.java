package com.docshare.document.repository;

import com.docshare.document.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    @Query("SELECT d FROM Document d WHERE d.ownerId = :userId ORDER BY d.updatedAt DESC")
    List<Document> findByOwnerId(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Document d JOIN DocumentPermission dp ON d.id = dp.documentId " +
           "WHERE dp.userId = :userId ORDER BY d.updatedAt DESC")
    List<Document> findSharedWithUser(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Document d WHERE d.id = :documentId AND " +
           "(d.ownerId = :userId OR d.isPublic = true OR " +
           "EXISTS (SELECT 1 FROM DocumentPermission dp WHERE dp.documentId = d.id AND dp.userId = :userId))")
    Optional<Document> findByIdAndUserHasAccess(@Param("documentId") Long documentId, @Param("userId") Long userId);
}
