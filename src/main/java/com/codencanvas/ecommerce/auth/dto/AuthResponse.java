package com.codencanvas.ecommerce.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private Long expiresIn;

}