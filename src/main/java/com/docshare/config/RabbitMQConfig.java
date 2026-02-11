package com.docshare.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {
    
    // Exchange names
    public static final String DOCUMENT_EXCHANGE = "docshare.document.exchange";
    public static final String AUTH_EXCHANGE = "docshare.auth.exchange";
    public static final String NOTIFICATION_EXCHANGE = "docshare.notification.exchange";
    
    // Queue names
    public static final String AUDIT_LOG_QUEUE = "docshare.audit.log.queue";
    public static final String DOCUMENT_AUDIT_QUEUE = "docshare.document.audit.queue";
    public static final String AUTH_AUDIT_QUEUE = "docshare.auth.audit.queue";
    public static final String NOTIFICATION_QUEUE = "docshare.notification.queue";
    
    // Routing keys
    public static final String DOCUMENT_CREATED_KEY = "document.created";
    public static final String DOCUMENT_UPDATED_KEY = "document.updated";
    public static final String DOCUMENT_DELETED_KEY = "document.deleted";
    public static final String DOCUMENT_SHARED_KEY = "document.shared";
    public static final String PERMISSION_REVOKED_KEY = "permission.revoked";
    
    public static final String USER_REGISTERED_KEY = "user.registered";
    public static final String USER_LOGIN_KEY = "user.login";
    public static final String USER_LOGOUT_KEY = "user.logout";
    public static final String LOGIN_FAILED_KEY = "login.failed";
    
    public static final String NOTIFICATION_SEND_KEY = "notification.send";
    
    // Message Converter
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
    
    // ==================== DOCUMENT EXCHANGE & QUEUES ====================
    
    @Bean
    public TopicExchange documentExchange() {
        return new TopicExchange(DOCUMENT_EXCHANGE);
    }
    
    @Bean
    public Queue documentAuditQueue() {
        return QueueBuilder.durable(DOCUMENT_AUDIT_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }
    
    @Bean
    public Binding documentCreatedBinding() {
        return BindingBuilder
                .bind(documentAuditQueue())
                .to(documentExchange())
                .with(DOCUMENT_CREATED_KEY);
    }
    
    @Bean
    public Binding documentUpdatedBinding() {
        return BindingBuilder
                .bind(documentAuditQueue())
                .to(documentExchange())
                .with(DOCUMENT_UPDATED_KEY);
    }
    
    @Bean
    public Binding documentDeletedBinding() {
        return BindingBuilder
                .bind(documentAuditQueue())
                .to(documentExchange())
                .with(DOCUMENT_DELETED_KEY);
    }
    
    @Bean
    public Binding documentSharedBinding() {
        return BindingBuilder
                .bind(documentAuditQueue())
                .to(documentExchange())
                .with(DOCUMENT_SHARED_KEY);
    }
    
    @Bean
    public Binding permissionRevokedBinding() {
        return BindingBuilder
                .bind(documentAuditQueue())
                .to(documentExchange())
                .with(PERMISSION_REVOKED_KEY);
    }
    
    // ==================== AUTH EXCHANGE & QUEUES ====================
    
    @Bean
    public TopicExchange authExchange() {
        return new TopicExchange(AUTH_EXCHANGE);
    }
    
    @Bean
    public Queue authAuditQueue() {
        return QueueBuilder.durable(AUTH_AUDIT_QUEUE)
                .withArgument("x-message-ttl", 86400000) // 24 hours TTL
                .build();
    }
    
    @Bean
    public Binding userRegisteredBinding() {
        return BindingBuilder
                .bind(authAuditQueue())
                .to(authExchange())
                .with(USER_REGISTERED_KEY);
    }
    
    @Bean
    public Binding userLoginBinding() {
        return BindingBuilder
                .bind(authAuditQueue())
                .to(authExchange())
                .with(USER_LOGIN_KEY);
    }
    
    @Bean
    public Binding userLogoutBinding() {
        return BindingBuilder
                .bind(authAuditQueue())
                .to(authExchange())
                .with(USER_LOGOUT_KEY);
    }
    
    @Bean
    public Binding loginFailedBinding() {
        return BindingBuilder
                .bind(authAuditQueue())
                .to(authExchange())
                .with(LOGIN_FAILED_KEY);
    }
    
    // ==================== NOTIFICATION EXCHANGE & QUEUES ====================
    
    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }
    
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-message-ttl", 3600000) // 1 hour TTL
                .build();
    }
    
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder
                .bind(notificationQueue())
                .to(notificationExchange())
                .with(NOTIFICATION_SEND_KEY);
    }
}
