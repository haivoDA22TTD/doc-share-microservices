package com.docshare.document.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisService {
    
    private final RedisTemplate<String, Object> redisTemplate;
    
    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
        log.debug("Set key: {}", key);
    }
    
    public void set(String key, Object value, Duration timeout) {
        redisTemplate.opsForValue().set(key, value, timeout);
        log.debug("Set key: {} with timeout: {}", key, timeout);
    }
    
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }
    
    public void delete(String key) {
        redisTemplate.delete(key);
        log.debug("Deleted key: {}", key);
    }
    
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
    
    public void expire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }
    
    public Long increment(String key) {
        return redisTemplate.opsForValue().increment(key);
    }
    
    public Long increment(String key, long delta) {
        return redisTemplate.opsForValue().increment(key, delta);
    }
}
