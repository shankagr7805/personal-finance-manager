package com.fintrack.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fintrack.entity.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Consolidated response DTOs to keep things tidy.
 */
public class Responses {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RegisterResponse {
        private String message;
        private Long userId;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MessageResponse {
        private String message;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionResponse {
        private Long id;
        private BigDecimal amount;
        private LocalDate date;
        private String category;
        private String description;
        private TransactionType type;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TransactionListResponse {
        private List<TransactionResponse> transactions;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryResponse {
        private String name;
        private TransactionType type;

        @JsonProperty("isCustom")
        private boolean custom;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CategoryListResponse {
        private List<CategoryResponse> categories;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalResponse {
        private Long id;
        private String goalName;
        private BigDecimal targetAmount;
        private LocalDate targetDate;
        private LocalDate startDate;
        private BigDecimal currentProgress;
        private Double progressPercentage;
        private BigDecimal remainingAmount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GoalListResponse {
        private List<GoalResponse> goals;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MonthlyReportResponse {
        private int month;
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class YearlyReportResponse {
        private int year;
        private Map<String, BigDecimal> totalIncome;
        private Map<String, BigDecimal> totalExpenses;
        private BigDecimal netSavings;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ErrorResponse {
        private String message;
        private int status;
    }
}
