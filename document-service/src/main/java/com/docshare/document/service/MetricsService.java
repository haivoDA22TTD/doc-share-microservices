package com.docshare.document.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
@Slf4j
public class MetricsService {
    
    private final MeterRegistry meterRegistry;
    
    private Counter documentCreatedCounter;
    private Counter documentEditedCounter;
    private Counter collaborationSessionCounter;
    
    private AtomicInteger activeWebSocketConnections = new AtomicInteger(0);
    private AtomicInteger activeCollaborationSessions = new AtomicInteger(0);
    
    private Timer documentSaveTimer;
    private Timer documentLoadTimer;
    
    @PostConstruct
    public void init() {
        documentCreatedCounter = Counter.builder("docshare.documents.created")
                .description("Total number of documents created")
                .register(meterRegistry);
        
        documentEditedCounter = Counter.builder("docshare.documents.edited")
                .description("Total number of document edits")
                .register(meterRegistry);
        
        collaborationSessionCounter = Counter.builder("docshare.collaboration.sessions")
                .description("Total number of collaboration sessions started")
                .register(meterRegistry);
        
        meterRegistry.gauge("docshare.websocket.connections.active", activeWebSocketConnections);
        meterRegistry.gauge("docshare.collaboration.sessions.active", activeCollaborationSessions);
        
        documentSaveTimer = Timer.builder("docshare.documents.save.time")
                .description("Time taken to save a document")
                .register(meterRegistry);
        
        documentLoadTimer = Timer.builder("docshare.documents.load.time")
                .description("Time taken to load a document")
                .register(meterRegistry);
        
        log.info("Metrics service initialized");
    }
    
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
    
    public void incrementCollaborationSession() {
        if (collaborationSessionCounter != null) {
            collaborationSessionCounter.increment();
        }
    }
    
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
