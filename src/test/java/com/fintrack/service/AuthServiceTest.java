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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;
    @InjectMocks private AuthService authService;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("test@example.com");
        registerRequest.setPassword("secret123");
        registerRequest.setFullName("Jane Doe");
        registerRequest.setPhoneNumber("+1234567890");

        loginRequest = new LoginRequest();
        loginRequest.setUsername("test@example.com");
        loginRequest.setPassword("secret123");
    }

    @Test
    void register_success() {
        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");
        User saved = User.builder().id(1L).username("test@example.com").build();
        when(userRepository.save(any())).thenReturn(saved);

        RegisterResponse response = authService.register(registerRequest);

        assertThat(response.getUserId()).isEqualTo(1L);
        assertThat(response.getMessage()).contains("successfully");
    }

    @Test
    void register_duplicateEmail_throwsConflict() {
        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);
        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void login_success() {
        when(authenticationManager.authenticate(any())).thenReturn(
                new UsernamePasswordAuthenticationToken("test@example.com", null));

        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession(true)).thenReturn(session);

        MessageResponse response = authService.login(loginRequest, httpRequest);
        assertThat(response.getMessage()).contains("successful");
    }

    @Test
    void login_badCredentials_throwsBadRequest() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad creds"));
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        assertThatThrownBy(() -> authService.login(loginRequest, httpRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void logout_invalidatesSession() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(httpRequest.getSession(false)).thenReturn(session);

        MessageResponse response = authService.logout(httpRequest);
        verify(session).invalidate();
        assertThat(response.getMessage()).contains("successful");
    }

    @Test
    void logout_noSession_noException() {
        HttpServletRequest httpRequest = mock(HttpServletRequest.class);
        when(httpRequest.getSession(false)).thenReturn(null);

        assertThatCode(() -> authService.logout(httpRequest)).doesNotThrowAnyException();
    }
}
