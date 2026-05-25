package com.fintrack.service;

import com.fintrack.dto.request.TransactionRequest;
import com.fintrack.dto.request.TransactionUpdateRequest;
import com.fintrack.dto.response.Responses.MessageResponse;
import com.fintrack.dto.response.Responses.TransactionListResponse;
import com.fintrack.dto.response.Responses.TransactionResponse;
import com.fintrack.entity.Category;
import com.fintrack.entity.Transaction;
import com.fintrack.entity.TransactionType;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ForbiddenException;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.CategoryRepository;
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
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private CategoryRepository categoryRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private TransactionService transactionService;

    private User user;
    private Category salary;
    private Transaction transaction;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@test.com").build();
        salary = Category.builder().id(1L).name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        transaction = Transaction.builder()
                .id(1L).amount(BigDecimal.valueOf(5000))
                .date(LocalDate.now().minusDays(1))
                .category(salary).user(user)
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
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.valueOf(5000));
        req.setDate(LocalDate.now().minusDays(1));
        req.setCategory("Salary");

        when(categoryRepository.findAccessibleByName("Salary", user)).thenReturn(Optional.of(salary));
        when(transactionRepository.save(any())).thenReturn(transaction);

        TransactionResponse response = transactionService.create(req);
        assertThat(response.getAmount()).isEqualTo(BigDecimal.valueOf(5000));
        assertThat(response.getType()).isEqualTo(TransactionType.INCOME);
    }

    @Test
    void create_futureDate_throwsBadRequest() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.TEN);
        req.setDate(LocalDate.now().plusDays(1));
        req.setCategory("Salary");

        assertThatThrownBy(() -> transactionService.create(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("future");
    }

    @Test
    void create_invalidCategory_throwsNotFound() {
        TransactionRequest req = new TransactionRequest();
        req.setAmount(BigDecimal.TEN);
        req.setDate(LocalDate.now());
        req.setCategory("Nonexistent");

        when(categoryRepository.findAccessibleByName("Nonexistent", user)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.create(req))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getAll_returnsTransactions() {
        when(transactionRepository.findByUserWithFilters(eq(user), any(), any(), any()))
                .thenReturn(List.of(transaction));

        TransactionListResponse response = transactionService.getAll(null, null, null);
        assertThat(response.getTransactions()).hasSize(1);
    }

    @Test
    void update_success() {
        TransactionUpdateRequest req = new TransactionUpdateRequest();
        req.setAmount(BigDecimal.valueOf(6000));

        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        when(transactionRepository.save(any())).thenReturn(transaction);

        TransactionResponse response = transactionService.update(1L, req);
        assertThat(response).isNotNull();
    }

    @Test
    void update_notOwner_throwsForbidden() {
        User other = User.builder().id(99L).build();
        Transaction other_tx = Transaction.builder().id(2L).user(other).category(salary).build();
        when(transactionRepository.findById(2L)).thenReturn(Optional.of(other_tx));

        assertThatThrownBy(() -> transactionService.update(2L, new TransactionUpdateRequest()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void delete_success() {
        when(transactionRepository.findById(1L)).thenReturn(Optional.of(transaction));
        MessageResponse response = transactionService.delete(1L);
        assertThat(response.getMessage()).contains("deleted");
        verify(transactionRepository).delete(transaction);
    }

    @Test
    void delete_notFound_throwsException() {
        when(transactionRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> transactionService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
