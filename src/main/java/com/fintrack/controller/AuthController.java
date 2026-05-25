package com.fintrack.controller;

import com.fintrack.dto.request.LoginRequest;
import com.fintrack.dto.request.RegisterRequest;
import com.fintrack.dto.response.Responses.MessageResponse;
import com.fintrack.dto.response.Responses.RegisterResponse;
import com.fintrack.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

/**
 * Handles user registration, login, and logout endpoints.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;

    /**
     * POST /api/auth/register
     * Creates a new user account.
     */
    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterRequest request) {
        RegisterResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/auth/login
     * Authenticates a user and creates a session cookie.
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContext context = SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        HttpSession session = httpRequest.getSession(true);

        session.setAttribute(
                HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                context);

        // FORCE SESSION CREATION
        session.setMaxInactiveInterval(1800);

        return ResponseEntity.ok()
                .header("Set-Cookie",
                        "JSESSIONID=" + session.getId() + "; Path=/; HttpOnly")
                .body(Map.of(
                        "message", "Login successful"));
    }

    /**
     * POST /api/auth/logout
     * Invalidates the current session.
     */
    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(HttpServletRequest httpRequest) {
        MessageResponse response = authService.logout(httpRequest);
        return ResponseEntity.ok(response);
    }
}
