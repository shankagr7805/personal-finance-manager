package com.fintrack.service;

import com.fintrack.dto.request.SavingsGoalRequest;
import com.fintrack.dto.request.SavingsGoalUpdateRequest;
import com.fintrack.dto.response.Responses.*;
import com.fintrack.entity.SavingsGoal;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ForbiddenException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.SavingsGoalRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

/**
 * Business logic for savings goals.
 * Progress is calculated dynamically from real transaction data.
 */
@Service
@RequiredArgsConstructor
public class SavingsGoalService {

    private final SavingsGoalRepository goalRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new savings goal for the current user.
     */
    public GoalResponse create(SavingsGoalRequest request) {
        User user = getCurrentUser();

        if (!request.getTargetDate().isAfter(LocalDate.now())) {
            throw new BadRequestException("Target date must be a future date");
        }

        LocalDate startDate = request.getStartDate() != null
                ? request.getStartDate()
                : LocalDate.now();

        SavingsGoal goal = SavingsGoal.builder()
                .goalName(request.getGoalName())
                .targetAmount(request.getTargetAmount())
                .targetDate(request.getTargetDate())
                .startDate(startDate)
                .user(user)
                .build();

        return toResponse(goalRepository.save(goal), user);
    }

    /**
     * Returns all savings goals for the current user, with live progress data.
     */
    public GoalListResponse getAll() {
        User user = getCurrentUser();
        List<GoalResponse> goals = goalRepository.findByUser(user).stream()
                .map(g -> toResponse(g, user))
                .toList();
        return GoalListResponse.builder().goals(goals).build();
    }

    /**
     * Returns a single savings goal by ID.
     */
    public GoalResponse getById(Long id) {
        User user = getCurrentUser();
        SavingsGoal goal = findGoalForUser(id, user);
        return toResponse(goal, user);
    }

    /**
     * Updates target amount and/or target date of an existing goal.
     */
    public GoalResponse update(Long id, SavingsGoalUpdateRequest request) {
        User user = getCurrentUser();
        SavingsGoal goal = findGoalForUser(id, user);

        if (request.getTargetDate() != null) {
            if (!request.getTargetDate().isAfter(LocalDate.now())) {
                throw new BadRequestException("Target date must be a future date");
            }
            goal.setTargetDate(request.getTargetDate());
        }
        if (request.getTargetAmount() != null) {
            goal.setTargetAmount(request.getTargetAmount());
        }

        return toResponse(goalRepository.save(goal), user);
    }

    /**
     * Deletes a savings goal by ID.
     */
    public MessageResponse delete(Long id) {
        User user = getCurrentUser();
        SavingsGoal goal = findGoalForUser(id, user);
        goalRepository.delete(goal);
        return MessageResponse.builder().message("Goal deleted successfully").build();
    }

    // --- Helpers ---

    private SavingsGoal findGoalForUser(Long id, User user) {
        SavingsGoal goal = goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Goal not found with id: " + id));
        if (!goal.getUser().getId().equals(user.getId())) {
            throw new ForbiddenException("You do not have permission to access this goal");
        }
        return goal;
    }

    private GoalResponse toResponse(SavingsGoal goal, User user) {
        BigDecimal progress = transactionRepository.calculateNetSavingsSince(user, goal.getStartDate());
        if (progress == null) progress = BigDecimal.ZERO;

        BigDecimal target = goal.getTargetAmount();
        double percentage = target.compareTo(BigDecimal.ZERO) == 0
                ? 0.0
                : progress.divide(target, 4, RoundingMode.HALF_UP)
                          .multiply(BigDecimal.valueOf(100))
                          .doubleValue();

        BigDecimal remaining = target.subtract(progress);

        return GoalResponse.builder()
                .id(goal.getId())
                .goalName(goal.getGoalName())
                .targetAmount(goal.getTargetAmount())
                .targetDate(goal.getTargetDate())
                .startDate(goal.getStartDate())
                .currentProgress(progress)
                .progressPercentage(Math.round(percentage * 100.0) / 100.0)
                .remainingAmount(remaining)
                .build();
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
