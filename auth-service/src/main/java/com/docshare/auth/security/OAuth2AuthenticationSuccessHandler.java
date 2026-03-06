package com.docshare.auth.security;

import com.docshare.auth.dto.OAuth2UserInfo;
import com.docshare.auth.entity.Role;
import com.docshare.auth.entity.User;
import com.docshare.auth.repository.RoleRepository;
import com.docshare.auth.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                       Authentication authentication) throws IOException, ServletException {
        
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        Map<String, Object> attributes = oAuth2User.getAttributes();
        
        OAuth2UserInfo userInfo = OAuth2UserInfo.fromGoogle(attributes);
        
        User user = userRepository.findByEmail(userInfo.getEmail())
                .orElseGet(() -> createNewUser(userInfo));
        
        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);
        
        String token = tokenProvider.generateTokenForOAuth2User(user);
        
        log.info("OAuth2 login successful for user: {}", user.getEmail());
        
        // Redirect to frontend with token
        String targetUrl = UriComponentsBuilder.fromUriString("http://localhost:4200/oauth2/redirect")
                .queryParam("token", token)
                .build().toUriString();
        
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
    
    private User createNewUser(OAuth2UserInfo userInfo) {
        User user = User.builder()
                .username(userInfo.getEmail().split("@")[0])
                .email(userInfo.getEmail())
                .fullName(userInfo.getName())
                .password("")
                .enabled(true)
                .accountNonLocked(true)
                .build();
        
        Role userRole = roleRepository.findByName(Role.RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role not found"));
        
        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        
        User savedUser = userRepository.save(user);
        
        log.info("Created new user from OAuth2: {}", savedUser.getEmail());
        
        return savedUser;
    }
}
