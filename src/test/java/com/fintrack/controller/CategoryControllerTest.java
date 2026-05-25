package com.fintrack.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for category management endpoints.
 */
class CategoryControllerTest extends BaseControllerTest {

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = loginAsNewUser();
    }

    @Test
    void getCategories_returnsDefaultsAndCustom() throws Exception {
        mockMvc.perform(get("/api/categories").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categories").isArray())
                // Salary should always be present (default)
                .andExpect(jsonPath("$.categories[?(@.name == 'Salary')]").exists());
    }

    @Test
    void createCustomCategory_success() throws Exception {
        String body = """
            { "name": "Freelance", "type": "INCOME" }
            """;
        mockMvc.perform(post("/api/categories").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Freelance"))
                .andExpect(jsonPath("$.isCustom").value(true));
    }

    @Test
    void createCustomCategory_duplicate_returns409() throws Exception {
        String body = """
            { "name": "SideGig", "type": "INCOME" }
            """;
        mockMvc.perform(post("/api/categories").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(post("/api/categories").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void createCustomCategory_shadowsDefault_returns409() throws Exception {
        String body = """
            { "name": "Salary", "type": "INCOME" }
            """;
        mockMvc.perform(post("/api/categories").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void deleteCustomCategory_success() throws Exception {
        // Create first
        mockMvc.perform(post("/api/categories").session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\": \"TempCat\", \"type\": \"EXPENSE\"}"));

        mockMvc.perform(delete("/api/categories/TempCat").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Category deleted successfully"));
    }

    @Test
    void deleteDefaultCategory_returns403() throws Exception {
        mockMvc.perform(delete("/api/categories/Salary").session(session))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteCategory_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/categories/NonExistent").session(session))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCategories_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isUnauthorized());
    }
}
