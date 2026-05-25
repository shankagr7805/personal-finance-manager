package com.fintrack.dto.request;

import com.fintrack.entity.TransactionType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * Request payload for creating a custom category.
 */
@Data
public class CategoryRequest {

    @NotBlank(message = "Category name is required")
    private String name;

    @NotNull(message = "Category type is required (INCOME or EXPENSE)")
    private TransactionType type;
}
