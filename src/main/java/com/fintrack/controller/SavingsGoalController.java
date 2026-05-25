package com.fintrack.controller;

import com.fintrack.dto.request.SavingsGoalRequest;
import com.fintrack.dto.request.SavingsGoalUpdateRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.service.SavingsGoalService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for savings goal management.
 */
@RestController
@RequestMapping("/api/goals")
@RequiredArgsConstructor
public class SavingsGoalController {

    private final SavingsGoalService goalService;

    /**
     * POST /api/goals
     * Creates a new savings goal.
     */
    @PostMapping
    public ResponseEntity<GoalResponse> create(@Valid @RequestBody SavingsGoalRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goalService.create(request));
    }

    /**
     * GET /api/goals
     * Returns all savings goals for the current user.
     */
    @GetMapping
    public ResponseEntity<GoalListResponse> getAll() {
        return ResponseEntity.ok(goalService.getAll());
    }

    /**
     * GET /api/goals/{id}
     * Returns a specific savings goal with current progress.
     */
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.getById(id));
    }

    /**
     * PUT /api/goals/{id}
     * Updates target amount and/or target date of a goal.
     */
    @PutMapping("/{id}")
    public ResponseEntity<GoalResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody SavingsGoalUpdateRequest request) {
        return ResponseEntity.ok(goalService.update(id, request));
    }

    /**
     * DELETE /api/goals/{id}
     * Deletes a savings goal.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(goalService.delete(id));
    }
}
