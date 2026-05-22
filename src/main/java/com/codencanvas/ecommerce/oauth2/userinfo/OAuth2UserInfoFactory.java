package com.codencanvas.ecommerce.oauth2.userinfo;

import java.util.Map;

import org.springframework.security.oauth2.core.OAuth2AuthenticationException;

public class OAuth2UserInfoFactory {

    public static OAuth2UserInfo getOAuth2UserInfo(String registrationId,
            Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "github" -> new GithubOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                    "Provider not supported: " + registrationId);
        };
    }
}
