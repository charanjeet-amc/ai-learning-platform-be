package com.ailearning.platform.dto.response;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AuthResponse {
    private String token;
    private String userId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private List<String> roles;
}
