package com.docshare.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RateLimitService {
    
    private final RedisService redisService;
    private final MetricsService metricsService;
    private static final String RATE_LIMIT_PREFIX = "ratelimit:";
    
    // Default: 10 requests per minute (for testing)
    private static final long DEFAULT_LIMIT = 10;
    private static final long DEFAULT_WINDOW_SECONDS = 60;
    
    public boolean isAllowed(String identifier) {
        return isAllowed(identifier, DEFAULT_LIMIT, DEFAULT_WINDOW_SECONDS);
    }
    
    public boolean isAllowed(String identifier, long limit, long windowSeconds) {
        String key = RATE_LIMIT_PREFIX + identifier;
        
        Long currentCount = redisService.increment(key);
        
        if (currentCount == null) {
            return false;
        }
        
        // Set expiration on first request
        if (currentCount == 1) {
            redisService.expire(key, windowSeconds, TimeUnit.SECONDS);
        }
        
        boolean allowed = currentCount <= limit;
        
        if (!allowed) {
            log.warn("Rate limit exceeded for: {} (count: {}, limit: {})", identifier, currentCount, limit);
            // Track rate limit exceeded metric
            metricsService.incrementRateLimitExceeded();
        }
        
        return allowed;
    }
    
    public long getRemainingRequests(String identifier, long limit) {
        String key = RATE_LIMIT_PREFIX + identifier;
        Object count = redisService.get(key);
        
        if (count == null) {
            return limit;
        }
        
        long currentCount = Long.parseLong(count.toString());
        return Math.max(0, limit - currentCount);
    }
    
    public void reset(String identifier) {
        String key = RATE_LIMIT_PREFIX + identifier;
        redisService.delete(key);
        log.info("Rate limit reset for: {}", identifier);
    }
}
