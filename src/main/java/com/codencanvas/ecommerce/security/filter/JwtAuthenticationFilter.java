package com.codencanvas.ecommerce.security.filter;

import java.io.IOException;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.codencanvas.ecommerce.security.service.JwtService;
import com.codencanvas.ecommerce.user.UserDetailsServiceImpl;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsServiceImpl userDetailsService;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        // 1. مفيش token — كمّل (endpoint ممكن يكون public)
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = authHeader.substring(7);

        try {
            // 2. في token — هنتحقق منه
            // لو في أي مشكلة → catch هيمسكها

            // 3. الـ token في الـ blacklist؟
            Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + token);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token has been revoked");
                return; // ← ارفض مش تكمل
            }

            // 4. استخرج الـ email — لو الـ token expired أو invalid هيرمي exception هنا
            String email = jwtService.extractEmail(token);

            // 5. حط الـ user في الـ SecurityContext لو مش موجود
            if (email != null &&
                SecurityContextHolder.getContext().getAuthentication() == null) {

                UserDetails userDetails = userDetailsService.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                    );

                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Token expired");
        } catch (JwtException e) {
            sendErrorResponse(response, HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private void sendErrorResponse(HttpServletResponse response,
                                   HttpStatus status,
                                   String message) throws IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
            String.format(
                "{\"status\": %d, \"error\": \"%s\", \"timestamp\": \"%s\"}",
                status.value(),
                message,
                java.time.LocalDateTime.now()
            )
        );
    }
}