package com.fintrack.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for transaction CRUD endpoints.
 */
class TransactionControllerTest extends BaseControllerTest {

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = loginAsNewUser();
    }

    @Test
    void createTransaction_success_returns201() throws Exception {
        String body = """
            {
              "amount": 5000.00,
              "date": "%s",
              "category": "Salary",
              "description": "Monthly salary"
            }
            """.formatted(LocalDate.now().minusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(5000.00))
                .andExpect(jsonPath("$.type").value("INCOME"))
                .andExpect(jsonPath("$.category").value("Salary"));
    }

    @Test
    void createTransaction_futureDate_returns400() throws Exception {
        String body = """
            { "amount": 100.00, "date": "%s", "category": "Salary" }
            """.formatted(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createTransaction_unknownCategory_returns404() throws Exception {
        String body = """
            { "amount": 100.00, "date": "%s", "category": "Unicorn" }
            """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void createTransaction_unauthenticated_returns401() throws Exception {
        String body = """
            { "amount": 100.00, "date": "%s", "category": "Salary" }
            """.formatted(LocalDate.now());

        mockMvc.perform(post("/api/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getTransactions_returnsAllForUser() throws Exception {
        // Create a transaction first
        String body = """
            { "amount": 200.00, "date": "%s", "category": "Food" }
            """.formatted(LocalDate.now());
        mockMvc.perform(post("/api/transactions").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(get("/api/transactions").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions[0].category").value("Food"));
    }

    @Test
    void getTransactions_differentUser_seesOwnDataOnly() throws Exception {
        MockHttpSession otherSession = loginAsNewUser();

        // User 1 creates a transaction
        String body = """
            { "amount": 9999.00, "date": "%s", "category": "Salary" }
            """.formatted(LocalDate.now());
        mockMvc.perform(post("/api/transactions").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        // User 2 should see no transactions
        mockMvc.perform(get("/api/transactions").session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isEmpty());
    }

    @Test
    void updateTransaction_success() throws Exception {
        // Create
        String createBody = """
            { "amount": 3000.00, "date": "%s", "category": "Salary" }
            """.formatted(LocalDate.now().minusDays(5));
        var createResult = mockMvc.perform(post("/api/transactions").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Update
        String updateBody = """
            { "amount": 4000.00, "description": "Raise!" }
            """;
        mockMvc.perform(put("/api/transactions/" + id).session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(4000.00))
                .andExpect(jsonPath("$.description").value("Raise!"));
    }

    @Test
    void updateTransaction_notOwner_returns403() throws Exception {
        // User 1 creates
        String createBody = """
            { "amount": 1000.00, "date": "%s", "category": "Salary" }
            """.formatted(LocalDate.now());
        var createResult = mockMvc.perform(post("/api/transactions").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andReturn();

        long id = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // User 2 tries to update
        MockHttpSession otherSession = loginAsNewUser();
        mockMvc.perform(put("/api/transactions/" + id).session(otherSession)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": 1.00}"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteTransaction_success() throws Exception {
        String createBody = """
            { "amount": 50.00, "date": "%s", "category": "Food" }
            """.formatted(LocalDate.now());
        var result = mockMvc.perform(post("/api/transactions").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/transactions/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Transaction deleted successfully"));
    }

    @Test
    void deleteTransaction_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/transactions/99999").session(session))
                .andExpect(status().isNotFound());
    }
}
