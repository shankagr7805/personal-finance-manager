package com.fintrack.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for report endpoints.
 */
class ReportControllerTest extends BaseControllerTest {

    private MockHttpSession session;
    private int currentYear;
    private int currentMonth;

    @BeforeEach
    void setUp() throws Exception {
        session = loginAsNewUser();
        currentYear = LocalDate.now().getYear();
        currentMonth = LocalDate.now().getMonthValue();

        // Seed a salary and food expense in current month
        addTransaction("5000.00", "Salary");
        addTransaction("300.00", "Food");
    }

    private void addTransaction(String amount, String category) throws Exception {
        String body = """
            { "amount": %s, "date": "%s", "category": "%s" }
            """.formatted(amount, LocalDate.now(), category);
        mockMvc.perform(post("/api/transactions").session(session)
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    @Test
    void monthlyReport_returnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/" + currentYear + "/" + currentMonth)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.month").value(currentMonth))
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.totalIncome.Salary").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(300.00))
                .andExpect(jsonPath("$.netSavings").value(4700.00));
    }

    @Test
    void yearlyReport_returnsCorrectData() throws Exception {
        mockMvc.perform(get("/api/reports/yearly/" + currentYear)
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.year").value(currentYear))
                .andExpect(jsonPath("$.totalIncome.Salary").value(5000.00))
                .andExpect(jsonPath("$.totalExpenses.Food").value(300.00))
                .andExpect(jsonPath("$.netSavings").value(4700.00));
    }

    @Test
    void monthlyReport_emptyMonth_returnsZeroSavings() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/" + (currentYear - 1) + "/1")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0));
    }

    @Test
    void report_unauthenticated_returns401() throws Exception {
        mockMvc.perform(get("/api/reports/monthly/" + currentYear + "/" + currentMonth))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void report_dataIsolation_otherUserSeesOwnData() throws Exception {
        MockHttpSession otherSession = loginAsNewUser();

        // Other user should see empty reports
        mockMvc.perform(get("/api/reports/monthly/" + currentYear + "/" + currentMonth)
                        .session(otherSession))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.netSavings").value(0));
    }
}
