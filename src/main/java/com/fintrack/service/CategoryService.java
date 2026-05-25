package com.fintrack.service;

import com.fintrack.dto.request.CategoryRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.entity.Category;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ConflictException;
import com.fintrack.exception.ForbiddenException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.CategoryRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Business logic for category management.
 * Merges default categories with user-specific custom ones.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Returns all categories accessible to the current user —
     * system defaults plus their own custom categories.
     */
    public CategoryListResponse getAll() {
        User user = getCurrentUser();

        List<Category> defaults = categoryRepository.findByIsCustomFalse();
        List<Category> custom = categoryRepository.findByUserAndIsCustomTrue(user);

        List<CategoryResponse> responses = new ArrayList<>();
        defaults.forEach(c -> responses.add(toResponse(c)));
        custom.forEach(c -> responses.add(toResponse(c)));

        return CategoryListResponse.builder().categories(responses).build();
    }

    /**
     * Creates a custom category for the current user.
     * Enforces unique name per user.
     */
    public CategoryResponse create(CategoryRequest request) {
        User user = getCurrentUser();

        if (categoryRepository.existsByNameAndUser(request.getName(), user)) {
            throw new ConflictException("You already have a category named '" + request.getName() + "'");
        }

        // Also prevent shadowing default category names
        if (categoryRepository.findByNameAndIsCustomFalse(request.getName()).isPresent()) {
            throw new ConflictException("'" + request.getName() + "' is a system default category name");
        }

        Category category = Category.builder()
                .name(request.getName())
                .type(request.getType())
                .isCustom(true)
                .user(user)
                .build();

        return toResponse(categoryRepository.save(category));
    }

    /**
     * Deletes a custom category by name.
     * Cannot delete default categories or categories in use by transactions.
     */
    public MessageResponse delete(String name) {
        User user = getCurrentUser();

        // Check if trying to delete a default category
        if (categoryRepository.findByNameAndIsCustomFalse(name).isPresent()) {
            throw new ForbiddenException("System default categories cannot be deleted");
        }

        Category category = categoryRepository.findByNameAndUser(name, user)
                .orElseThrow(() -> new ResourceNotFoundException("Custom category not found: " + name));

        if (transactionRepository.existsByCategory(category)) {
            throw new BadRequestException(
                    "Cannot delete category '" + name + "' because it is referenced by existing transactions");
        }

        categoryRepository.delete(category);
        return MessageResponse.builder().message("Category deleted successfully").build();
    }

    private CategoryResponse toResponse(Category c) {

        return CategoryResponse.builder()
                .name(c.getName())
                .type(c.getType())
                .custom(c.isCustom())
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
