package com.codencanvas.ecommerce.user.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileRequest {

    @Size(max = 100, message = "Full name must be less than 100 characters")
    private String fullName;

    private String avatarUrl;
}