package com.codencanvas.ecommerce.infrastructure.ratelimit;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        String clientIp = getClientIp(request);

        LimitType limitType = getLimitType(path);

        if (limitType != null) {
            if (!rateLimitService.isAllowed(clientIp, limitType)) {
                sendRateLimitResponse(response, limitType);
                return;
            }
        }

        if (!rateLimitService.isAllowed(clientIp, LimitType.GENERAL)) {
            sendRateLimitResponse(response, LimitType.GENERAL);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private LimitType getLimitType(String path) {
        if (path.contains("/login"))
            return LimitType.LOGIN;
        if (path.contains("/register"))
            return LimitType.REGISTER;
        if (path.contains("/forgot-password"))
            return LimitType.FORGOT_PASSWORD;
        return null;
    }

    private boolean isTrustedProxy(String ip) {
        return ip.startsWith("10.0.") || ip.startsWith("172.31.");
    }

    private String getClientIp(HttpServletRequest request) {

        String remoteAddr = request.getRemoteAddr();

        if (isTrustedProxy(remoteAddr)) {
            String xfHeader = request.getHeader("X-Forwarded-For");
            if (xfHeader != null) {
                String[] ips = xfHeader.split(",");
                return ips[ips.length - 2].trim();
            }
        }

        return remoteAddr;
    }

    private void sendRateLimitResponse(HttpServletResponse response, LimitType type)
            throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        String message = switch (type) {
            case LOGIN -> "Too many login attempts. Please try again later.";
            case REGISTER -> "Too many registration attempts. Please try again later.";
            case FORGOT_PASSWORD -> "Too many password reset requests. Please try again later.";
            case GENERAL -> "Too many requests. Please slow down.";
        };

        var error = java.util.Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 429,
                "error", "Too Many Requests",
                "message", message);

        objectMapper.writeValue(response.getWriter(), error);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator")
                || path.startsWith("/login/oauth2")
                || path.startsWith("/oauth2")
                || path.startsWith("/error");
    }

}
