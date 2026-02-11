package com.docshare.consumer;

import com.docshare.config.RabbitMQConfig;
import com.docshare.event.AuthEvent;
import com.docshare.event.DocumentEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditLogConsumer {
    
    @RabbitListener(queues = RabbitMQConfig.DOCUMENT_AUDIT_QUEUE)
    public void handleDocumentAudit(DocumentEvent event) {
        try {
            log.info("📥 [AUDIT] Document Event: {} | Document: {} (ID: {}) | User: {} (ID: {}) | Time: {}", 
                    event.getEventType(),
                    event.getDocumentTitle(),
                    event.getDocumentId(),
                    event.getUsername(),
                    event.getUserId(),
                    event.getTimestamp());
            
            // TODO: Save to audit log database table
            // auditLogRepository.save(convertToAuditLog(event));
            
        } catch (Exception e) {
            log.error("❌ Failed to process document audit event: {}", event.getEventId(), e);
            throw e; // Re-throw to trigger retry
        }
    }
    
    @RabbitListener(queues = RabbitMQConfig.AUTH_AUDIT_QUEUE)
    public void handleAuthAudit(AuthEvent event) {
        try {
            log.info("📥 [AUDIT] Auth Event: {} | User: {} (ID: {}) | Success: {} | Time: {}", 
                    event.getEventType(),
                    event.getUsername(),
                    event.getUserId(),
                    event.isSuccess(),
                    event.getTimestamp());
            
            if (!event.isSuccess()) {
                log.warn("⚠️ Failed auth attempt: {} - Reason: {}", 
                        event.getUsername(), event.getFailureReason());
            }
            
            // TODO: Save to audit log database table
            // auditLogRepository.save(convertToAuditLog(event));
            
        } catch (Exception e) {
            log.error("❌ Failed to process auth audit event: {}", event.getEventId(), e);
            throw e; // Re-throw to trigger retry
        }
    }
}
