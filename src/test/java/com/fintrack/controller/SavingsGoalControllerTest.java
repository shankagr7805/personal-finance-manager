package com.fintrack.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for savings goal endpoints.
 */
class SavingsGoalControllerTest extends BaseControllerTest {

    private MockHttpSession session;

    @BeforeEach
    void setUp() throws Exception {
        session = loginAsNewUser();
    }

    private String futureDate(int monthsAhead) {
        return LocalDate.now().plusMonths(monthsAhead).toString();
    }

    @Test
    void createGoal_success() throws Exception {
        String body = """
            {
              "goalName": "Emergency Fund",
              "targetAmount": 5000.00,
              "targetDate": "%s"
            }
            """.formatted(futureDate(6));

        mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.goalName").value("Emergency Fund"))
                .andExpect(jsonPath("$.targetAmount").value(5000.00))
                .andExpect(jsonPath("$.currentProgress").isNumber())
                .andExpect(jsonPath("$.progressPercentage").isNumber())
                .andExpect(jsonPath("$.remainingAmount").isNumber());
    }

    @Test
    void createGoal_pastTargetDate_returns400() throws Exception {
        String body = """
            { "goalName": "Old Goal", "targetAmount": 1000.00, "targetDate": "2020-01-01" }
            """;
        mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAllGoals_returnsGoals() throws Exception {
        String body = """
            { "goalName": "Vacation", "targetAmount": 2000.00, "targetDate": "%s" }
            """.formatted(futureDate(3));
        mockMvc.perform(post("/api/goals").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body));

        mockMvc.perform(get("/api/goals").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goals").isArray())
                .andExpect(jsonPath("$.goals[0].goalName").value("Vacation"));
    }

    @Test
    void getGoalById_success() throws Exception {
        String body = """
            { "goalName": "Car Fund", "targetAmount": 10000.00, "targetDate": "%s" }
            """.formatted(futureDate(12));
        var result = mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(get("/api/goals/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.goalName").value("Car Fund"));
    }

    @Test
    void getGoalById_notOwner_returns403() throws Exception {
        String body = """
            { "goalName": "Private Goal", "targetAmount": 500.00, "targetDate": "%s" }
            """.formatted(futureDate(2));
        var result = mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        MockHttpSession otherSession = loginAsNewUser();
        mockMvc.perform(get("/api/goals/" + id).session(otherSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateGoal_success() throws Exception {
        String createBody = """
            { "goalName": "House", "targetAmount": 20000.00, "targetDate": "%s" }
            """.formatted(futureDate(24));
        var result = mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(createBody))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        String updateBody = """
            { "targetAmount": 25000.00, "targetDate": "%s" }
            """.formatted(futureDate(30));
        mockMvc.perform(put("/api/goals/" + id).session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.targetAmount").value(25000.00));
    }

    @Test
    void deleteGoal_success() throws Exception {
        String body = """
            { "goalName": "Temp Goal", "targetAmount": 100.00, "targetDate": "%s" }
            """.formatted(futureDate(1));
        var result = mockMvc.perform(post("/api/goals").session(session)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andReturn();

        long id = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(delete("/api/goals/" + id).session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Goal deleted successfully"));
    }

    @Test
    void deleteGoal_notFound_returns404() throws Exception {
        mockMvc.perform(delete("/api/goals/99999").session(session))
                .andExpect(status().isNotFound());
    }
}
