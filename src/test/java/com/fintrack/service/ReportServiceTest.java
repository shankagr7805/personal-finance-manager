package com.fintrack.service;

import com.fintrack.dto.response.Responses.MonthlyReportResponse;
import com.fintrack.dto.response.Responses.YearlyReportResponse;
import com.fintrack.entity.TransactionType;
import com.fintrack.entity.User;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ReportService reportService;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("user@test.com").build();
        Authentication auth = mock(Authentication.class);
        when(auth.getName()).thenReturn("user@test.com");
        SecurityContext ctx = mock(SecurityContext.class);
        when(ctx.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(ctx);
        when(userRepository.findByUsername("user@test.com")).thenReturn(Optional.of(user));
    }

    @Test
    void getMonthlyReport_returnsCorrectData() {
        List<Object[]> incomeRows = new ArrayList<>();
        incomeRows.add(new Object[]{"Salary", BigDecimal.valueOf(3000)});

        List<Object[]> expenseRows = new ArrayList<>();
        expenseRows.add(new Object[]{"Food", BigDecimal.valueOf(500)});

        when(transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.INCOME, 2024, 1))
                .thenReturn(incomeRows);
        when(transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.EXPENSE, 2024, 1))
                .thenReturn(expenseRows);

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 1);

        assertThat(response.getMonth()).isEqualTo(1);
        assertThat(response.getYear()).isEqualTo(2024);
        assertThat(response.getTotalIncome()).containsEntry("Salary", BigDecimal.valueOf(3000));
        assertThat(response.getTotalExpenses()).containsEntry("Food", BigDecimal.valueOf(500));
        assertThat(response.getNetSavings()).isEqualTo(BigDecimal.valueOf(2500));
    }

    @Test
    void getYearlyReport_returnsCorrectData() {
        List<Object[]> incomeRows = new ArrayList<>();
        incomeRows.add(new Object[]{"Salary", BigDecimal.valueOf(36000)});

        List<Object[]> expenseRows = new ArrayList<>();
        expenseRows.add(new Object[]{"Rent", BigDecimal.valueOf(12000)});

        when(transactionRepository.sumByCategoryAndTypeAndYear(user, TransactionType.INCOME, 2024))
                .thenReturn(incomeRows);
        when(transactionRepository.sumByCategoryAndTypeAndYear(user, TransactionType.EXPENSE, 2024))
                .thenReturn(expenseRows);

        YearlyReportResponse response = reportService.getYearlyReport(2024);

        assertThat(response.getYear()).isEqualTo(2024);
        assertThat(response.getNetSavings()).isEqualTo(BigDecimal.valueOf(24000));
    }

    @Test
    void getMonthlyReport_emptyData_returnsZeroSavings() {
        when(transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.INCOME, 2024, 6))
                .thenReturn(new ArrayList<>());
        when(transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.EXPENSE, 2024, 6))
                .thenReturn(new ArrayList<>());

        MonthlyReportResponse response = reportService.getMonthlyReport(2024, 6);
        assertThat(response.getNetSavings()).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
