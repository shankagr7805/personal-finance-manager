package com.fintrack.service;

import com.fintrack.dto.response.Responses.MonthlyReportResponse;
import com.fintrack.dto.response.Responses.YearlyReportResponse;
import com.fintrack.entity.TransactionType;
import com.fintrack.entity.User;
import com.fintrack.exception.ResourceNotFoundException;
import com.fintrack.repository.TransactionRepository;
import com.fintrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates monthly and yearly financial reports for the authenticated user.
 */
@Service
@RequiredArgsConstructor
public class ReportService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    /**
     * Generates a report breaking down income and expenses by category for a given month.
     *
     * @param year  the 4-digit year
     * @param month the month (1–12)
     */
    public MonthlyReportResponse getMonthlyReport(int year, int month) {
        User user = getCurrentUser();

        Map<String, BigDecimal> incomeMap = buildCategoryMap(
                transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.INCOME, year, month));

        Map<String, BigDecimal> expenseMap = buildCategoryMap(
                transactionRepository.sumByCategoryAndTypeAndYearAndMonth(user, TransactionType.EXPENSE, year, month));

        BigDecimal totalIncome = incomeMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return MonthlyReportResponse.builder()
                .month(month)
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    /**
     * Generates an aggregate report for a full year, summing all category amounts.
     *
     * @param year the 4-digit year
     */
    public YearlyReportResponse getYearlyReport(int year) {
        User user = getCurrentUser();

        Map<String, BigDecimal> incomeMap = buildCategoryMap(
                transactionRepository.sumByCategoryAndTypeAndYear(user, TransactionType.INCOME, year));

        Map<String, BigDecimal> expenseMap = buildCategoryMap(
                transactionRepository.sumByCategoryAndTypeAndYear(user, TransactionType.EXPENSE, year));

        BigDecimal totalIncome = incomeMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = expenseMap.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal netSavings = totalIncome.subtract(totalExpenses);

        return YearlyReportResponse.builder()
                .year(year)
                .totalIncome(incomeMap)
                .totalExpenses(expenseMap)
                .netSavings(netSavings)
                .build();
    }

    private Map<String, BigDecimal> buildCategoryMap(List<Object[]> rows) {
        Map<String, BigDecimal> map = new LinkedHashMap<>();
        for (Object[] row : rows) {
            String categoryName = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            map.put(categoryName, amount);
        }
        return map;
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Authenticated user not found"));
    }
}
