package com.fintrack.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request payload for updating a savings goal.
 * Only targetAmount and targetDate can be modified.
 */
@Data
public class SavingsGoalUpdateRequest {

    @DecimalMin(value = "0.01", message = "Target amount must be greater than zero")
    private BigDecimal targetAmount;

    private LocalDate targetDate;
}
