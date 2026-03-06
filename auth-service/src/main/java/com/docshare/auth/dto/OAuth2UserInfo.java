package com.docshare.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OAuth2UserInfo {
    
    private String id;
    private String email;
    private String name;
    private String picture;
    private String provider;
    
    public static OAuth2UserInfo fromGoogle(Map<String, Object> attributes) {
        return OAuth2UserInfo.builder()
                .id((String) attributes.get("sub"))
                .email((String) attributes.get("email"))
                .name((String) attributes.get("name"))
                .picture((String) attributes.get("picture"))
                .provider("google")
                .build();
    }
}
