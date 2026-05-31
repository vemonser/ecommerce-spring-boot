package com.codencanvas.ecommerce.common.exception;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.codencanvas.ecommerce.common.dto.ApiResponse;
import com.codencanvas.ecommerce.common.util.LanguageUtils;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
        private final MessageSource messageSource;

        private String translate(String key, String fallback) {
                return messageSource.getMessage(key, null, fallback, LanguageUtils.getCurrentLocale());
        }

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

        @ExceptionHandler(AppException.class)
        public ResponseEntity<ApiResponse<Void>> handleAppException(
                AppException ex, HttpServletRequest request) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.error(translate(ex.getMessageKey(), ex.getMessageKey()), ex.getStatus(), request.getRequestURI()));
        }

        @ExceptionHandler(BadCredentialsException.class)
        public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
                BadCredentialsException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(translate("error.auth.bad_credentials", "Invalid credentials"), 401, request.getRequestURI()));
        }

        @ExceptionHandler(DisabledException.class)
        public ResponseEntity<ApiResponse<Void>> handleDisabled(
                        DisabledException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error(translate("error.auth.disabled", "Account is disabled"), 401, request.getRequestURI()));
        }

        @ExceptionHandler({EmailAlreadyExistsException.class, UsernameAlreadyExistsException.class})
        public ResponseEntity<ApiResponse<Void>> handleConflict(
                RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(translate("error.auth.conflict", ex.getMessage()), 409, request.getRequestURI()));
        }

        @ExceptionHandler(InvalidTokenException.class)
        public ResponseEntity<ApiResponse<Void>> handleInvalidToken(
                InvalidTokenException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(translate("error.auth.invalid_token", ex.getMessage()), 400, request.getRequestURI()));
        }

        @ExceptionHandler(WeakPasswordException.class)
        public ResponseEntity<ApiResponse<Void>> handleWeakPassword(
                WeakPasswordException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(translate("error.auth.weak_password", ex.getMessage()), 400, request.getRequestURI()));
        }


        @ExceptionHandler(OAuth2ProcessingException.class)
        public ResponseEntity<ApiResponse<Void>> handleOAuth2(
                OAuth2ProcessingException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(translate("error.auth.oauth2", ex.getMessage()), 400, request.getRequestURI()));
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<Void>> handleGeneric(
                Exception ex, HttpServletRequest request) {
        log.error("Unexpected error at {}", request.getRequestURI(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(translate("error.generic", "An unexpected error occurred"), 500, request.getRequestURI()));
        }

        @ExceptionHandler(AccountLockoutException.class)
        public ResponseEntity<ApiResponse<Void>> handleAccountLockout(
                AccountLockoutException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.LOCKED)
                .body(ApiResponse.error(translate("error.auth.account_locked", ex.getMessage()), 423, request.getRequestURI()));
        }
        
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<ApiResponse<Void>> handleEntityNotFound(
                EntityNotFoundException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(translate("error.not_found", ex.getMessage()), 404, request.getRequestURI()));
        }

        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<ApiResponse<Void>> handleIllegalState(
                IllegalStateException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(translate("error.illegal_state", ex.getMessage()), 409, request.getRequestURI()));
        }

}