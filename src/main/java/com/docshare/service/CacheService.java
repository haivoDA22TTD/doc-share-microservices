package com.docshare.service;

import com.docshare.dto.DocumentDto;
import com.docshare.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {
    
    private final RedisService redisService;
    
    private static final String USER_CACHE_PREFIX = "cache:user:";
    private static final String DOCUMENT_CACHE_PREFIX = "cache:document:";
    private static final Duration DEFAULT_TTL = Duration.ofMinutes(30);
    
    // User cache
    public void cacheUser(Long userId, UserDto user) {
        String key = USER_CACHE_PREFIX + userId;
        redisService.set(key, user, DEFAULT_TTL);
        log.debug("Cached user: {}", userId);
    }
    
    public UserDto getCachedUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        Object cached = redisService.get(key);
        if (cached != null) {
            log.debug("Cache hit for user: {}", userId);
            return (UserDto) cached;
        }
        log.debug("Cache miss for user: {}", userId);
        return null;
    }
    
    public void evictUser(Long userId) {
        String key = USER_CACHE_PREFIX + userId;
        redisService.delete(key);
        log.debug("Evicted user cache: {}", userId);
    }
    
    // Document cache
    public void cacheDocument(Long documentId, DocumentDto document) {
        String key = DOCUMENT_CACHE_PREFIX + documentId;
        redisService.set(key, document, DEFAULT_TTL);
        log.debug("Cached document: {}", documentId);
    }
    
    public DocumentDto getCachedDocument(Long documentId) {
        String key = DOCUMENT_CACHE_PREFIX + documentId;
        Object cached = redisService.get(key);
        if (cached != null) {
            log.debug("Cache hit for document: {}", documentId);
            return (DocumentDto) cached;
        }
        log.debug("Cache miss for document: {}", documentId);
        return null;
    }
    
    public void evictDocument(Long documentId) {
        String key = DOCUMENT_CACHE_PREFIX + documentId;
        redisService.delete(key);
        log.debug("Evicted document cache: {}", documentId);
    }
}
