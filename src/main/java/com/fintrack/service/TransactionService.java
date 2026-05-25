package com.fintrack.service;

import com.fintrack.dto.request.TransactionRequest;
import com.fintrack.dto.request.TransactionUpdateRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.entity.Category;
import com.fintrack.entity.Transaction;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ForbiddenException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for transaction management.
 * Enforces validation rules and data isolation per user.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new transaction for the authenticated user.
     */
    public TransactionResponse create(TransactionRequest request) {
        User user = getCurrentUser();

        if (request.getDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Transaction date cannot be in the future");
        }

        Category category = categoryRepository.findAccessibleByName(request.getCategory(), user)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Category not found: " + request.getCategory()));

        Transaction transaction = Transaction.builder()
                .amount(request.getAmount())
                .date(request.getDate())
                .category(category)
                .description(request.getDescription())
                .user(user)
                .build();

        Transaction saved = transactionRepository.save(transaction);
        return toResponse(saved);
    }

    /**
     * Returns all transactions for the current user, with optional filters.
     */
    public TransactionListResponse getAll(LocalDate startDate, LocalDate endDate, String categoryName) {
        User user = getCurrentUser();

        Category categoryFilter = null;

        if (categoryName != null) {
            categoryFilter = categoryRepository
                    .findAccessibleByName(categoryName, user)
                    .orElseThrow(() ->
                            new ResourceNotFoundException("Category not found"));
        }

        List<Transaction> transactions = transactionRepository.findByUserWithFilters(
                user, startDate, endDate, categoryFilter);

        List<TransactionResponse> responses = transactions.stream()
                .map(this::toResponse)
                .toList();

        return TransactionListResponse.builder().transactions(responses).build();
    }

    /**
     * Updates an existing transaction. Date field cannot be changed.
     */
    public TransactionResponse update(Long id, TransactionUpdateRequest request) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to update this transaction");
        }

        if (request.getAmount() != null) {
            transaction.setAmount(request.getAmount());
        }
        if (request.getCategory() != null) {
            Category category = categoryRepository.findAccessibleByName(request.getCategory(), user)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Category not found: " + request.getCategory()));
            transaction.setCategory(category);
        }
        if (request.getDescription() != null) {
            transaction.setDescription(request.getDescription());
        }

        return toResponse(transactionRepository.save(transaction));
    }

    /**
     * Deletes a transaction by ID.
     */
    public MessageResponse delete(Long id) {
        User user = getCurrentUser();

        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to delete this transaction");
        }

        transactionRepository.delete(transaction);
        return MessageResponse.builder().message("Transaction deleted successfully").build();
    }

    private TransactionResponse toResponse(Transaction t) {
        return TransactionResponse.builder()
                .id(t.getId())
                .amount(t.getAmount())
                .date(t.getDate())
                .category(t.getCategory().getName())
                .description(t.getDescription())
                .type(t.getCategory().getType())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
