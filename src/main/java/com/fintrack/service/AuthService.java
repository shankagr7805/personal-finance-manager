package com.fintrack.service;

import com.fintrack.dto.request.LoginRequest;
import com.fintrack.dto.request.RegisterRequest;
import com.fintrack.dto.response.Responses.MessageResponse;
import com.fintrack.dto.response.Responses.RegisterResponse;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ConflictException;
import com.fintrack.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Service;

/**
 * Handles user registration, login, and logout workflows.
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user after validating email uniqueness.
     *
     * @param request registration details
     * @return response containing the new user's ID
     */
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("An account with this email already exists");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        User saved = userRepository.save(user);
        return RegisterResponse.builder()
                .message("User registered successfully")
                .userId(saved.getId())
                .build();
    }

    /**
     * Authenticates a user and creates a session.
     *
     * @param request login credentials
     * @param httpRequest the HTTP request (used to create session)
     * @return success message response
     */
    public MessageResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // Bind auth to session
            HttpSession session = httpRequest.getSession(true);
            session.setAttribute(
                    HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY,
                    SecurityContextHolder.getContext()
            );
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid username or password");
        }

        return MessageResponse.builder().message("Login successful").build();
    }

    /**
     * Invalidates the current user's session.
     *
     * @param httpRequest the HTTP request containing the session
     * @return success message response
     */
    public MessageResponse logout(HttpServletRequest httpRequest) {
        HttpSession session = httpRequest.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
        return MessageResponse.builder().message("Logout successful").build();
    }
}
