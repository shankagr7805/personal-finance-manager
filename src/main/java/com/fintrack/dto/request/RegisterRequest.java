package com.fintrack.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * Request payload for user registration.
 */
@Data
public class RegisterRequest {

    @NotBlank(message = "Username is required")
    @Email(message = "Username must be a valid email address")
    private String username;

    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&_#^()\\-+=])[A-Za-z\\d@$!%*?&_#^()\\-+=]{8,}$",
        message = "Password must be at least 8 characters with one uppercase, one lowercase, one digit, and one special character"
    )
    private String password;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^\\+?[1-9]\\d{6,14}$",
        message = "Phone number must be valid (e.g. +919876543210)"
    )
    private String phoneNumber;
}