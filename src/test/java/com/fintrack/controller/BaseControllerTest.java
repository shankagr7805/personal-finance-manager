package com.fintrack.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintrack.dto.request.LoginRequest;
import com.fintrack.dto.request.RegisterRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Base class for controller integration tests.
 * Provides shared helpers for registering and logging in test users.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class BaseControllerTest {

    @Autowired
    protected MockMvc mockMvc;
    @Autowired
    protected ObjectMapper objectMapper;

    private int userCounter = 0;

    /**
     * Registers a user and returns the session cookie to use in subsequent
     * requests.
     */
    protected MockHttpSession registerAndLogin(String email) throws Exception {

        RegisterRequest reg = new RegisterRequest();

        reg.setUsername(email);
        reg.setPassword("Password@123");
        reg.setFullName("Test User");
        reg.setPhoneNumber("+919876543210");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(reg)))
                .andExpect(status().isCreated());

        LoginRequest login = new LoginRequest();

        login.setUsername(email);
        login.setPassword("Password@123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result
                .getRequest()
                .getSession(false);
    }

    /** Convenience method — generates a unique email and logs in. */
    protected MockHttpSession loginAsNewUser() throws Exception {
        return registerAndLogin("user" + (++userCounter) + "@test.com");
    }
}