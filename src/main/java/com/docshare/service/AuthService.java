package com.docshare.service;

import com.docshare.dto.AuthResponse;
import com.docshare.dto.LoginRequest;
import com.docshare.dto.RegisterRequest;
import com.docshare.entity.Role;
import com.docshare.entity.User;
import com.docshare.repository.RoleRepository;
import com.docshare.repository.UserRepository;
import com.docshare.security.JwtTokenProvider;
import com.docshare.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;
    private final TokenBlacklistService tokenBlacklistService;
    private final MetricsService metricsService;
    private final EventPublisher eventPublisher;
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        String token = tokenProvider.generateToken(authentication);
        
        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
        
        // Update last login
        User user = userRepository.findById(userPrincipal.getId()).orElseThrow();
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        Set<String> roles = userPrincipal.getAuthorities().stream()
                .map(item -> item.getAuthority())
                .collect(Collectors.toSet());
        
        // Track login metric
        metricsService.incrementUserLogin();
        
        // Publish login event
        eventPublisher.publishUserLogin(userPrincipal.getId(), userPrincipal.getUsername(), userPrincipal.getEmail());
        
        return AuthResponse.builder()
                .token(token)
                .userId(userPrincipal.getId())
                .username(userPrincipal.getUsername())
                .email(userPrincipal.getEmail())
                .roles(roles)
                .build();
    }
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .enabled(true)
                .accountNonLocked(true)
                .build();
        
        // Assign default role
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        // Publish user registered event
        eventPublisher.publishUserRegistered(savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());
        
        // Auto login after register
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        
        String token = tokenProvider.generateToken(authentication);
        
        return AuthResponse.builder()
                .token(token)
                .userId(savedUser.getId())
                .username(savedUser.getUsername())
                .email(savedUser.getEmail())
                .roles(savedUser.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .build();
    }
    
    public void logout(String token) {
        try {
            // Get token expiration time
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            Date expiration = claims.getExpiration();
            Date now = new Date();
            
            // Calculate remaining time until expiration
            long remainingTime = expiration.getTime() - now.getTime();
            
            if (remainingTime > 0) {
                Duration duration = Duration.ofMillis(remainingTime);
                tokenBlacklistService.blacklistToken(token, duration);
                
                // Track logout and token blacklist metrics
                metricsService.incrementUserLogout();
                metricsService.incrementTokenBlacklisted();
                
                // Publish logout event
                String username = claims.getSubject();
                Long userId = claims.get("userId", Long.class);
                String email = claims.get("email", String.class);
                eventPublisher.publishUserLogout(userId, username, email);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to logout: " + e.getMessage());
        }
    }
}
