package com.codencanvas.ecommerce.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.codencanvas.ecommerce.auth.security.filter.JwtAuthenticationFilter;
import com.codencanvas.ecommerce.auth.security.handler.CustomAccessDeniedHandler;
import com.codencanvas.ecommerce.auth.security.handler.CustomAuthEntryPoint;
import com.codencanvas.ecommerce.common.config.SecurityHeadersConfig;
import com.codencanvas.ecommerce.infrastructure.ratelimit.RateLimitFilter;
import com.codencanvas.ecommerce.oauth2.handler.OAuth2SuccessHandler;
import com.codencanvas.ecommerce.oauth2.service.OAuth2UserServiceImpl;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
        private final JwtAuthenticationFilter jwtAuthFilter;
        // private final UserDetailsServiceImpl userDetailsService;
        private final CustomAuthEntryPoint authEntryPoint;
        private final CustomAccessDeniedHandler accessDeniedHandler;
        private final OAuth2SuccessHandler oAuth2SuccessHandler;
        private final OAuth2UserServiceImpl oAuth2UserService;
        private final SecurityHeadersConfig securityHeadersConfig;
        private final RateLimitFilter rateLimitFilter;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .addFilterBefore(rateLimitFilter, UsernamePasswordAuthenticationFilter.class)
                                .headers(
                                                securityHeadersConfig.securityHeaders())
                                .csrf(csrf -> csrf.disable())
                                // 2. CORS
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                                // 3. الـ Endpoints اللي مش محتاجة authentication
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers("/api/v1/auth/**").permitAll()
                                                .requestMatchers(HttpMethod.GET, "/api/v1/products/**").permitAll()
                                                .requestMatchers("/login/oauth2/code/**", "/oauth2/**").permitAll()
                                                .requestMatchers("/actuator/health").permitAll()
                                                .requestMatchers("/error").permitAll()
                                                .anyRequest().authenticated())

                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                                .oauth2Login(oauth2 -> oauth2
                                                .userInfoEndpoint(info -> info
                                                                .userService(oAuth2UserService))
                                                .successHandler(oAuth2SuccessHandler))

                                // 5. حط الـ JWT Filter قبل Spring's built-in filter
                                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)

                                // 6. الـ Error Handlers
                                .exceptionHandling(ex -> ex
                                                .authenticationEntryPoint(authEntryPoint) // 401
                                                .accessDeniedHandler(accessDeniedHandler) // 403
                                );

                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration config = new CorsConfiguration();
                // هتجيب الـ origin من env variable
                config.setAllowedOrigins(List.of(
                                System.getenv().getOrDefault("FRONTEND_URL", "http://localhost:3000")));
                config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                config.setAllowedHeaders(List.of("Authorization", "Content-Type", "X-Requested-With"));
                config.setExposedHeaders(List.of("X-Refresh-Token"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", config);
                return source;
        }

        @Bean
        public AuthenticationManager authenticationManager(
                        AuthenticationConfiguration config) throws Exception {
                return config.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return Argon2PasswordEncoder.defaultsForSpringSecurity_v5_8();
        }

}
