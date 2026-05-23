package com.codencanvas.ecommerce.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.codencanvas.ecommerce.common.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<Map<String, String>>> handleValidationErrors(
                        MethodArgumentNotValidException ex, HttpServletRequest request) {

                Map<String, String> errors = new LinkedHashMap<>();
                ex.getBindingResult().getFieldErrors()
                                .forEach(err -> errors.put(err.getField(), err.getDefaultMessage()));

                return ResponseEntity.badRequest()
                                .body(ApiResponse.errorWithData("Validation failed", 400, request.getRequestURI(),
                                                errors));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                        BadCredentialsException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error("Invalid email/username or password", 401,
                                                request.getRequestURI()));
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiResponse<Void>> handleDisabled(
                        DisabledException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body(ApiResponse.error(ex.getMessage(), 401, request.getRequestURI()));
        }

        @ExceptionHandler({ EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class })
        public ResponseEntity<ApiResponse<Void>> handleConflict(
                        RuntimeException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body(ApiResponse.error(ex.getMessage(), 409, request.getRequestURI()));
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
                        InvalidTokenException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), 400, request.getRequestURI()));
        }

        @ExceptionHandler(WeakPasswordException.class)
        public ResponseEntity<ApiResponse<Void>> handleWeakPassword(
                        WeakPasswordException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), 400, request.getRequestURI()));
        }

        @ExceptionHandler(OAuth2ProcessingException.class)
        public ResponseEntity<ApiResponse<Void>> handleOAuth2(
                        OAuth2ProcessingException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(ApiResponse.error(ex.getMessage(), 400, request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneric(
                        Exception ex, HttpServletRequest request) {
                log.error("Unexpected error at {}", request.getRequestURI(), ex);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body(ApiResponse.error("An unexpected error occurred", 500, request.getRequestURI()));
        }

        @ExceptionHandler(AccountLockoutException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccountLockout(
                        AccountLockoutException ex, HttpServletRequest request) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                                .body(ApiResponse.error(ex.getMessage(), 423, request.getRequestURI()));
        }

}