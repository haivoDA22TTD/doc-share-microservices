package com.docshare.document.service;

import com.docshare.document.dto.DocumentDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisService redisService;
    private static final String DOCUMENT_CACHE_PREFIX = "document:";
    private static final Duration CACHE_TTL = Duration.ofHours(1);
    
    public void cacheDocument(DocumentDto document) {
        String key = DOCUMENT_CACHE_PREFIX + document.getId();
        redisService.set(key, document, CACHE_TTL);
        log.debug("Cached document: {}", document.getId());
    }
    
    public DocumentDto getCachedDocument(Long documentId) {
        String key = DOCUMENT_CACHE_PREFIX + documentId;
        Object cached = redisService.get(key);
        if (cached instanceof DocumentDto) {
            log.debug("Cache hit for document: {}", documentId);
            return (DocumentDto) cached;
        }
        log.debug("Cache miss for document: {}", documentId);
        return null;
    }
    
    public void evictDocument(Long documentId) {
        String key = DOCUMENT_CACHE_PREFIX + documentId;
        redisService.delete(key);
        log.debug("Evicted document from cache: {}", documentId);
    }
}
