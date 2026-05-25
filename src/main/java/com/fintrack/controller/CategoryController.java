package com.fintrack.controller;

import com.fintrack.dto.request.CategoryRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for category management.
 */
@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * GET /api/categories
     * Returns all categories (defaults + user's custom categories).
     */
    @GetMapping
    public ResponseEntity<CategoryListResponse> getAll() {
        return ResponseEntity.ok(categoryService.getAll());
    }

    /**
     * POST /api/categories
     * Creates a new custom category for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<?> create(
            @Valid @RequestBody CategoryRequest request) {

        CategoryResponse category = categoryService.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(java.util.Map.of(
                        "name", category.getName(),
                        "type", category.getType(),
                        "isCustom", true));
    }

    /**
     * DELETE /api/categories/{name}
     * Deletes a custom category by name.
     * Cannot delete system defaults or categories in use.
     */
    @DeleteMapping("/{name}")
    public ResponseEntity<MessageResponse> delete(@PathVariable String name) {
        return ResponseEntity.ok(categoryService.delete(name));
    }
}
