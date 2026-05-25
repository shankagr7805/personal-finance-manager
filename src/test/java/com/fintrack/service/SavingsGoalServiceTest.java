package com.fintrack.service;

import com.fintrack.dto.request.SavingsGoalRequest;
import com.fintrack.dto.request.SavingsGoalUpdateRequest;
import com.fintrack.dto.response.Responses.GoalListResponse;
import com.fintrack.dto.response.Responses.GoalResponse;
import com.fintrack.dto.response.Responses.MessageResponse;
import com.fintrack.entity.SavingsGoal;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ForbiddenException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.SavingsGoalRepository;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SavingsGoalServiceTest {

    @Mock private SavingsGoalRepository goalRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private SavingsGoalService goalService;

    private User user;
    private SavingsGoal goal;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@test.com").build();
        goal = SavingsGoal.builder()
                .id(1L)
                .goalName("Emergency Fund")
                .targetAmount(BigDecimal.valueOf(5000))
                .targetDate(LocalDate.now().plusMonths(6))
                .startDate(LocalDate.now())
                .user(user)
                .build();

        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);

        when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void create_success() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setGoalName("Emergency Fund");
        req.setTargetAmount(BigDecimal.valueOf(5000));
        req.setTargetDate(LocalDate.now().plusMonths(6));

        when(goalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.calculateNetSavingsSince(eq(user), any())).thenReturn(BigDecimal.valueOf(1000));

        GoalResponse response = goalService.create(req);
        assertThat(response.getGoalName()).isEqualTo("Emergency Fund");
        assertThat(response.getCurrentProgress()).isEqualTo(BigDecimal.valueOf(1000));
    }

    @Test
    void create_pastTargetDate_throwsBadRequest() {
        SavingsGoalRequest req = new SavingsGoalRequest();
        req.setGoalName("Test");
        req.setTargetAmount(BigDecimal.valueOf(100));
        req.setTargetDate(LocalDate.now().minusDays(1));

        assertThatThrownBy(() -> goalService.create(req))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void getAll_returnsGoals() {
        when(goalRepository.findByUser(user)).thenReturn(List.of(goal));
        when(transactionRepository.calculateNetSavingsSince(eq(user), any())).thenReturn(BigDecimal.ZERO);

        GoalListResponse response = goalService.getAll();
        assertThat(response.getGoals()).hasSize(1);
    }

    @Test
    void getById_notOwner_throwsForbidden() {
        User other = User.builder().id(99L).build();
        SavingsGoal otherGoal = SavingsGoal.builder().id(2L).user(other).build();
        when(goalRepository.findById(2L)).thenReturn(Optional.of(otherGoal));

        assertThatThrownBy(() -> goalService.getById(2L))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void update_success() {
        SavingsGoalUpdateRequest req = new SavingsGoalUpdateRequest();
        req.setTargetAmount(BigDecimal.valueOf(8000));
        req.setTargetDate(LocalDate.now().plusYears(1));

        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        when(goalRepository.save(any())).thenReturn(goal);
        when(transactionRepository.calculateNetSavingsSince(eq(user), any())).thenReturn(BigDecimal.ZERO);

        GoalResponse response = goalService.update(1L, req);
        assertThat(response).isNotNull();
    }

    @Test
    void delete_success() {
        when(goalRepository.findById(1L)).thenReturn(Optional.of(goal));
        MessageResponse response = goalService.delete(1L);
        assertThat(response.getMessage()).contains("deleted");
        verify(goalRepository).delete(goal);
    }

    @Test
    void delete_notFound_throwsException() {
        when(goalRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> goalService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
