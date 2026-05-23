package com.codencanvas.ecommerce.user.dto;

import com.codencanvas.ecommerce.user.model.Role;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserProfileResponse {
    private Long id;
    private String email;
    private String username;
    private String fullName;
    private String avatarUrl;
    private Role role;
    private boolean enabled;
}