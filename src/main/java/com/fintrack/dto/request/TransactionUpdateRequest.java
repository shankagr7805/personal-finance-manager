package com.fintrack.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Request payload for updating an existing transaction.
 * Date cannot be changed after creation per business rules.
 */
@Data
public class TransactionUpdateRequest {

    @DecimalMin(value = "0.01", message = "Amount must be greater than zero")
    private BigDecimal amount;

    private String category;

    private String description;
}
