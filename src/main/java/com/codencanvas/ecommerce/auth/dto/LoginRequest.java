package com.codencanvas.ecommerce.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class LoginRequest {

    @NotBlank(message = "Email or username is required")
    private String identifier; // email أو username

    @NotBlank(message = "Password is required")
    private String password;
}
