package com.codencanvas.ecommerce.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityHeadersConfig {
        public Customizer<HeadersConfigurer<HttpSecurity>> securityHeaders() {
                return headers -> headers
                                // Clickjacking
                                .frameOptions(frame -> frame.deny())
                                // MIME sniffing
                                .contentTypeOptions(Customizer.withDefaults())
                                // HTTPS only
                                // .httpStrictTransportSecurity(hsts -> hsts
                                // .maxAgeInSeconds(31536000)
                                // .includeSubDomains(true)
                                // .preload(true))
                                // Referrer policy
                                .referrerPolicy(referrer -> referrer.policy(
                                                ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                                // CSP
                                .contentSecurityPolicy(csp -> csp
                                                .policyDirectives(
                                                                "default-src 'self'; " +
                                                                                "script-src 'self'; " +
                                                                                "style-src 'self' 'unsafe-inline'; " +
                                                                                "img-src 'self' data: https:; " +
                                                                                "font-src 'self'; " +
                                                                                "connect-src 'self'; " +
                                                                                "frame-ancestors 'none'; " +
                                                                                "base-uri 'self'; " +
                                                                                "form-action 'self';"));
        }

}
