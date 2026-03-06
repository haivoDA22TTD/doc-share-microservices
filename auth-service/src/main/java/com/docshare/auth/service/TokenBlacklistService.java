package com.docshare.auth.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistService {
    
    private final RedisService redisService;
    private static final String BLACKLIST_PREFIX = "blacklist:token:";
    
    public void blacklistToken(String token, Duration expiration) {
        String key = BLACKLIST_PREFIX + token;
        redisService.set(key, "blacklisted", expiration);
        log.info("Token blacklisted: {} for duration: {}", token.substring(0, 10) + "...", expiration);
    }
    
    public boolean isBlacklisted(String token) {
        String key = BLACKLIST_PREFIX + token;
        return Boolean.TRUE.equals(redisService.hasKey(key));
    }
    
    public void removeFromBlacklist(String token) {
        String key = BLACKLIST_PREFIX + token;
        redisService.delete(key);
        log.info("Token removed from blacklist: {}", token.substring(0, 10) + "...");
    }
}
