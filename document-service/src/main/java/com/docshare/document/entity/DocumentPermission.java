package com.docshare.document.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "document_permissions", 
       uniqueConstraints = @UniqueConstraint(columnNames = {"document_id", "user_id"}))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentPermission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "document_id", nullable = false)
    private Long documentId;
    
    @Column(name = "user_id", nullable = false)
    private Long userId;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PermissionType permission;
    
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime grantedAt;
    
    public enum PermissionType {
        VIEW,
        EDIT
    }
}
