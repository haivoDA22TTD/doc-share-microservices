package com.docshare.consumer;

import com.docshare.config.RabbitMQConfig;
import com.docshare.event.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {
    
    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleNotification(NotificationEvent event) {
        try {
            log.info("📥 [NOTIFICATION] Type: {} | Recipient: {} | Title: {} | Time: {}", 
                    event.getType(),
                    event.getRecipientUsername(),
                    event.getTitle(),
                    event.getTimestamp());
            
            // TODO: Implement notification logic
            switch (event.getType()) {
                case DOCUMENT_SHARED:
                    handleDocumentSharedNotification(event);
                    break;
                case DOCUMENT_EDITED:
                    handleDocumentEditedNotification(event);
                    break;
                case PERMISSION_GRANTED:
                    handlePermissionGrantedNotification(event);
                    break;
                case PERMISSION_REVOKED:
                    handlePermissionRevokedNotification(event);
                    break;
                default:
                    log.warn("Unknown notification type: {}", event.getType());
            }
            
        } catch (Exception e) {
            log.error("❌ Failed to process notification: {}", event.getEventId(), e);
            throw e; // Re-throw to trigger retry
        }
    }
    
    private void handleDocumentSharedNotification(NotificationEvent event) {
        log.info("📧 Sending 'Document Shared' notification to: {}", event.getRecipientEmail());
        // TODO: Send email or push notification
        // emailService.sendDocumentSharedEmail(event);
    }
    
    private void handleDocumentEditedNotification(NotificationEvent event) {
        log.info("📧 Sending 'Document Edited' notification to: {}", event.getRecipientEmail());
        // TODO: Send email or push notification
        // emailService.sendDocumentEditedEmail(event);
    }
    
    private void handlePermissionGrantedNotification(NotificationEvent event) {
        log.info("📧 Sending 'Permission Granted' notification to: {}", event.getRecipientEmail());
        // TODO: Send email or push notification
    }
    
    private void handlePermissionRevokedNotification(NotificationEvent event) {
        log.info("📧 Sending 'Permission Revoked' notification to: {}", event.getRecipientEmail());
        // TODO: Send email or push notification
    }
}
