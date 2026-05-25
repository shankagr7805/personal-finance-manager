package com.fintrack.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Request payload for user login.
 */
@Data
public class LoginRequest {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
