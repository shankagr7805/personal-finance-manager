package com.fintrack.controller;

import com.fintrack.dto.request.TransactionRequest;
import com.fintrack.dto.request.TransactionUpdateRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

/**
 * REST endpoints for transaction management (CRUD + filtering).
 */
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    /**
     * POST /api/transactions
     * Creates a new transaction for the authenticated user.
     */
    @PostMapping
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.create(request));
    }

    /**
     * GET /api/transactions
     * Returns all transactions, optionally filtered by date range and category.
     */
    @GetMapping

    public ResponseEntity<TransactionListResponse> getAll(

            @RequestParam(required = false)

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

            LocalDate startDate,

            @RequestParam(required = false)

            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)

            LocalDate endDate,

            @RequestParam(required = false) String category) {

        return ResponseEntity.ok(

                transactionService.getAll(startDate, endDate, category));

    }

    /**
     * PUT /api/transactions/{id}
     * Updates an existing transaction. Date cannot be changed.
     */
    @PutMapping("/{id}")
    public ResponseEntity<TransactionResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TransactionUpdateRequest request) {
        return ResponseEntity.ok(transactionService.update(id, request));
    }

    /**
     * DELETE /api/transactions/{id}
     * Deletes a transaction by ID.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        return ResponseEntity.ok(transactionService.delete(id));
    }
}
