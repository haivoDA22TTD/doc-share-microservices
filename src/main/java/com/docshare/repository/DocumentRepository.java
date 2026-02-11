package com.docshare.repository;

import com.docshare.entity.Document;
import com.docshare.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    
    List<Document> findByOwnerOrderByUpdatedAtDesc(User owner);
    
    @Query("SELECT d FROM Document d WHERE d.owner.id = :userId ORDER BY d.updatedAt DESC")
    List<Document> findByOwnerId(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Document d JOIN DocumentPermission dp ON d.id = dp.document.id " +
           "WHERE dp.user.id = :userId ORDER BY d.updatedAt DESC")
    List<Document> findSharedWithUser(@Param("userId") Long userId);
    
    @Query("SELECT d FROM Document d WHERE d.id = :documentId AND " +
           "(d.owner.id = :userId OR d.isPublic = true OR " +
           "EXISTS (SELECT 1 FROM DocumentPermission dp WHERE dp.document.id = d.id AND dp.user.id = :userId))")
    Optional<Document> findByIdAndUserHasAccess(@Param("documentId") Long documentId, @Param("userId") Long userId);
}
