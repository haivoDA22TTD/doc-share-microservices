package com.docshare.document.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserClient {
    
    private final RestTemplate restTemplate = new RestTemplate();
    private static final String AUTH_SERVICE_URL = "http://auth-service:8081";
    
    public Map<String, Object> getUserById(Long userId, String token) {
        try {
            String url = AUTH_SERVICE_URL + "/api/users/" + userId;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user from auth-service: {}", e.getMessage());
            Map<String, Object> fallback = new HashMap<>();
            fallback.put("id", userId);
            fallback.put("username", "User" + userId);
            return fallback;
        }
    }
    
    public Map<String, Object> getUserByUsername(String username, String token) {
        try {
            String url = AUTH_SERVICE_URL + "/api/users?username=" + username;
            
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            
            org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(headers);
            
            org.springframework.http.ResponseEntity<Map> response = restTemplate.exchange(
                url,
                org.springframework.http.HttpMethod.GET,
                entity,
                Map.class
            );
            
            return response.getBody();
        } catch (Exception e) {
            log.error("Error fetching user by username from auth-service: {}", e.getMessage());
            return null;
        }
    }
}
