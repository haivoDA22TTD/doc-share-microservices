package com.docshare.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    // Counters
    private Counter documentCreatedCounter;
    private Counter documentEditedCounter;
    private Counter userLoginCounter;
    private Counter userLogoutCounter;
    private Counter collaborationSessionCounter;
    private Counter rateLimitExceededCounter;
    private Counter tokenBlacklistedCounter;
    
    // Gauges
    private AtomicInteger activeWebSocketConnections = new AtomicInteger(0);
    private AtomicInteger activeCollaborationSessions = new AtomicInteger(0);
    
    // Timers
    private Timer documentSaveTimer;
    private Timer documentLoadTimer;
    
    public void init() {
        // Initialize counters
        documentCreatedCounter = Counter.builder("docshare.documents.created")
                .description("Total number of documents created")
                .register(meterRegistry);
        
        documentEditedCounter = Counter.builder("docshare.documents.edited")
                .description("Total number of document edits")
                .register(meterRegistry);
        
        userLoginCounter = Counter.builder("docshare.users.login")
                .description("Total number of user logins")
                .register(meterRegistry);
        
        userLogoutCounter = Counter.builder("docshare.users.logout")
                .description("Total number of user logouts")
                .register(meterRegistry);
        
        collaborationSessionCounter = Counter.builder("docshare.collaboration.sessions")
                .description("Total number of collaboration sessions started")
                .register(meterRegistry);
        
        rateLimitExceededCounter = Counter.builder("docshare.ratelimit.exceeded")
                .description("Total number of rate limit violations")
                .register(meterRegistry);
        
        tokenBlacklistedCounter = Counter.builder("docshare.tokens.blacklisted")
                .description("Total number of tokens blacklisted")
                .register(meterRegistry);
        
        // Initialize gauges
        meterRegistry.gauge("docshare.websocket.connections.active", activeWebSocketConnections);
        meterRegistry.gauge("docshare.collaboration.sessions.active", activeCollaborationSessions);
        
        // Initialize timers
        documentSaveTimer = Timer.builder("docshare.documents.save.time")
                .description("Time taken to save a document")
                .register(meterRegistry);
        
        documentLoadTimer = Timer.builder("docshare.documents.load.time")
                .description("Time taken to load a document")
                .register(meterRegistry);
        
        log.info("Metrics service initialized");
    }
    
    // Counter methods
    public void incrementDocumentCreated() {
        if (documentCreatedCounter != null) {
            documentCreatedCounter.increment();
        }
    }
    
    public void incrementDocumentEdited() {
        if (documentEditedCounter != null) {
            documentEditedCounter.increment();
        }
    }
    
    public void incrementUserLogin() {
        if (userLoginCounter != null) {
            userLoginCounter.increment();
        }
    }
    
    public void incrementUserLogout() {
        if (userLogoutCounter != null) {
            userLogoutCounter.increment();
        }
    }
    
    public void incrementCollaborationSession() {
        if (collaborationSessionCounter != null) {
            collaborationSessionCounter.increment();
        }
    }
    
    public void incrementRateLimitExceeded() {
        if (rateLimitExceededCounter != null) {
            rateLimitExceededCounter.increment();
        }
    }
    
    public void incrementTokenBlacklisted() {
        if (tokenBlacklistedCounter != null) {
            tokenBlacklistedCounter.increment();
        }
    }
    
    // Gauge methods
    public void incrementActiveWebSocketConnections() {
        activeWebSocketConnections.incrementAndGet();
    }
    
    public void decrementActiveWebSocketConnections() {
        activeWebSocketConnections.decrementAndGet();
    }
    
    public void incrementActiveCollaborationSessions() {
        activeCollaborationSessions.incrementAndGet();
    }
    
    public void decrementActiveCollaborationSessions() {
        activeCollaborationSessions.decrementAndGet();
    }
    
    // Timer methods
    public Timer.Sample startDocumentSaveTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopDocumentSaveTimer(Timer.Sample sample) {
        if (sample != null && documentSaveTimer != null) {
            sample.stop(documentSaveTimer);
        }
    }
    
    public Timer.Sample startDocumentLoadTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopDocumentLoadTimer(Timer.Sample sample) {
        if (sample != null && documentLoadTimer != null) {
            sample.stop(documentLoadTimer);
        }
    }
}
