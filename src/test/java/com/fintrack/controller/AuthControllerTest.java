package com.fintrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.dto.request.LoginRequest;
import com.fintrack.dto.request.RegisterRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for authentication endpoints.
 * Spins up the full application context with an in-memory H2 database.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    // ---- Register ----

    @Test
    void register_validRequest_returns201() throws Exception {
        RegisterRequest req = buildRegisterRequest("alice@example.com");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("User registered successfully"))
                .andExpect(jsonPath("$.userId").isNumber());
    }

    @Test
    void register_duplicateEmail_returns409() throws Exception {
        RegisterRequest req = buildRegisterRequest("bob@example.com");
        String body = objectMapper.writeValueAsString(req);

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void register_invalidEmail_returns400() throws Exception {
        RegisterRequest req = buildRegisterRequest("not-an-email");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_missingPassword_returns400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setUsername("test@example.com");
        req.setFullName("Test User");
        req.setPhoneNumber("+919876543210");

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    // ---- Login ----

    @Test
    void login_validCredentials_returns200WithCookie() throws Exception {
        RegisterRequest reg = buildRegisterRequest("carol@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("carol@example.com");
        login.setPassword("Password@123");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(cookie().exists("JSESSIONID"));
    }

    @Test
    void login_wrongPassword_returns401() throws Exception {
        RegisterRequest reg = buildRegisterRequest("dave@example.com");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();
        login.setUsername("dave@example.com");
        login.setPassword("Password@999");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isUnauthorized());
    }

    private RegisterRequest buildRegisterRequest(String email) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(email);
        req.setPassword("Password@123");
        req.setFullName("Test User");
        req.setPhoneNumber("+919876543210");
        return req;
    }
}