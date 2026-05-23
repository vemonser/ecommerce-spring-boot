package com.codencanvas.ecommerce.common.util;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.codencanvas.ecommerce.common.dto.ApiResponse;

import jakarta.servlet.http.HttpServletRequest;

public class ResponseUtil {

    public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> ok(String message, T data) {
        return ResponseEntity.ok(ApiResponse.success(message, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success("Created successfully", data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> accepted() {
        return ResponseEntity.status(HttpStatus.ACCEPTED)
            .body(ApiResponse.success("Request accepted", null));
    }

    public static <T> ResponseEntity<ApiResponse<T>> noContent() {
        return ResponseEntity.noContent().build();
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(
            HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status)
            .body(ApiResponse.<T>error(message, status.value(), request.getRequestURI()));
    }
}
