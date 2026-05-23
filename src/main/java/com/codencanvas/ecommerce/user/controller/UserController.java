package com.codencanvas.ecommerce.user.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.codencanvas.ecommerce.common.dto.ApiResponse;
import com.codencanvas.ecommerce.common.util.ResponseUtil;
import com.codencanvas.ecommerce.user.dto.UpdateProfileRequest;
import com.codencanvas.ecommerce.user.dto.UserProfileResponse;
import com.codencanvas.ecommerce.user.security.UserPrincipal;
import com.codencanvas.ecommerce.user.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseUtil.ok(userService.getProfile(principal.getUser()));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        return ResponseUtil.ok(userService.updateProfile(principal.getUser(), request));
    }

}
