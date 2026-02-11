package com.docshare.service;

import com.docshare.config.RabbitMQConfig;
import com.docshare.event.AuthEvent;
import com.docshare.event.DocumentEvent;
import com.docshare.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventPublisher {
    
    private final RabbitTemplate rabbitTemplate;
    
    // ==================== DOCUMENT EVENTS ====================
    
    public void publishDocumentCreated(Long documentId, String documentTitle, Long userId, String username, String email) {
        DocumentEvent event = DocumentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(DocumentEvent.EventType.DOCUMENT_CREATED)
                .documentId(documentId)
                .documentTitle(documentTitle)
                .userId(userId)
                .username(username)
                .userEmail(email)
                .timestamp(LocalDateTime.now())
                .build();
        
        publishDocumentEvent(RabbitMQConfig.DOCUMENT_CREATED_KEY, event);
    }
    
    public void publishDocumentUpdated(Long documentId, String documentTitle, Long userId, String username, String email) {
        DocumentEvent event = DocumentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(DocumentEvent.EventType.DOCUMENT_UPDATED)
                .documentId(documentId)
                .documentTitle(documentTitle)
                .userId(userId)
                .username(username)
                .userEmail(email)
                .timestamp(LocalDateTime.now())
                .build();
        
        publishDocumentEvent(RabbitMQConfig.DOCUMENT_UPDATED_KEY, event);
    }
    
    public void publishDocumentDeleted(Long documentId, String documentTitle, Long userId, String username, String email) {
        DocumentEvent event = DocumentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(DocumentEvent.EventType.DOCUMENT_DELETED)
                .documentId(documentId)
                .documentTitle(documentTitle)
                .userId(userId)
                .username(username)
                .userEmail(email)
                .timestamp(LocalDateTime.now())
                .build();
        
        publishDocumentEvent(RabbitMQConfig.DOCUMENT_DELETED_KEY, event);
    }
    
    public void publishDocumentShared(Long documentId, String documentTitle, Long ownerId, String ownerUsername, 
                                     Long sharedWithUserId, String sharedWithUsername, String permission) {
        DocumentEvent event = DocumentEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(DocumentEvent.EventType.DOCUMENT_SHARED)
                .documentId(documentId)
                .documentTitle(documentTitle)
                .userId(ownerId)
                .username(ownerUsername)
                .timestamp(LocalDateTime.now())
                .build();
        
        publishDocumentEvent(RabbitMQConfig.DOCUMENT_SHARED_KEY, event);
    }
    
    private void publishDocumentEvent(String routingKey, DocumentEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.DOCUMENT_EXCHANGE, routingKey, event);
            log.info("📤 Published document event: {} - Document ID: {}", event.getEventType(), event.getDocumentId());
        } catch (Exception e) {
            log.error("❌ Failed to publish document event: {}", event.getEventType(), e);
        }
    }
    
    // ==================== AUTH EVENTS ====================
    
    public void publishUserRegistered(Long userId, String username, String email) {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(AuthEvent.EventType.USER_REGISTERED)
                .userId(userId)
                .username(username)
                .email(email)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
        
        publishAuthEvent(RabbitMQConfig.USER_REGISTERED_KEY, event);
    }
    
    public void publishUserLogin(Long userId, String username, String email) {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(AuthEvent.EventType.USER_LOGIN)
                .userId(userId)
                .username(username)
                .email(email)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
        
        publishAuthEvent(RabbitMQConfig.USER_LOGIN_KEY, event);
    }
    
    public void publishUserLogout(Long userId, String username, String email) {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(AuthEvent.EventType.USER_LOGOUT)
                .userId(userId)
                .username(username)
                .email(email)
                .timestamp(LocalDateTime.now())
                .success(true)
                .build();
        
        publishAuthEvent(RabbitMQConfig.USER_LOGOUT_KEY, event);
    }
    
    public void publishLoginFailed(String username, String reason) {
        AuthEvent event = AuthEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .eventType(AuthEvent.EventType.LOGIN_FAILED)
                .username(username)
                .timestamp(LocalDateTime.now())
                .success(false)
                .failureReason(reason)
                .build();
        
        publishAuthEvent(RabbitMQConfig.LOGIN_FAILED_KEY, event);
    }
    
    private void publishAuthEvent(String routingKey, AuthEvent event) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.AUTH_EXCHANGE, routingKey, event);
            log.info("📤 Published auth event: {} - User: {}", event.getEventType(), event.getUsername());
        } catch (Exception e) {
            log.error("❌ Failed to publish auth event: {}", event.getEventType(), e);
        }
    }
    
    // ==================== NOTIFICATION EVENTS ====================
    
    public void publishNotification(NotificationEvent notification) {
        try {
            rabbitTemplate.convertAndSend(RabbitMQConfig.NOTIFICATION_EXCHANGE, 
                    RabbitMQConfig.NOTIFICATION_SEND_KEY, notification);
            log.info("📤 Published notification: {} - Recipient: {}", 
                    notification.getType(), notification.getRecipientUsername());
        } catch (Exception e) {
            log.error("❌ Failed to publish notification", e);
        }
    }
}
