package com.fintrack.repository;

import com.fintrack.entity.Category;
import com.fintrack.entity.Transaction;
import com.fintrack.entity.TransactionType;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Data access layer for Transaction entities.
 * Provides filtering, aggregation, and reporting queries.
 */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    /** All transactions for a user, newest first. */
    List<Transaction> findByUserOrderByDateDesc(User user);

    /** Filter by date range and/or category. */
    @Query("""
        SELECT t FROM Transaction t
        WHERE t.user = :user
          AND (:startDate IS NULL OR t.date >= :startDate)
          AND (:endDate IS NULL OR t.date <= :endDate)
          AND (:category IS NULL OR t.category = :category)
        ORDER BY t.date DESC
    """)
    List<Transaction> findByUserWithFilters(
            @Param("user") User user,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("category") Category category
    );

    /** Sum income or expenses for a specific month/year. */
    @Query("""
        SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t
        WHERE t.user = :user
          AND t.category.type = :type
          AND YEAR(t.date) = :year
          AND MONTH(t.date) = :month
    """)
    BigDecimal sumByUserAndTypeAndYearAndMonth(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month
    );

    /** Sum income/expenses per category for monthly report. */
    @Query("""
        SELECT t.category.name, COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user = :user
          AND t.category.type = :type
          AND YEAR(t.date) = :year
          AND MONTH(t.date) = :month
        GROUP BY t.category.name
    """)
    List<Object[]> sumByCategoryAndTypeAndYearAndMonth(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("year") int year,
            @Param("month") int month
    );

    /** Sum income/expenses per category for yearly report. */
    @Query("""
        SELECT t.category.name, COALESCE(SUM(t.amount), 0)
        FROM Transaction t
        WHERE t.user = :user
          AND t.category.type = :type
          AND YEAR(t.date) = :year
        GROUP BY t.category.name
    """)
    List<Object[]> sumByCategoryAndTypeAndYear(
            @Param("user") User user,
            @Param("type") TransactionType type,
            @Param("year") int year
    );

    /** Net savings (income minus expenses) since a given date — used for goals. */
    @Query("""
        SELECT COALESCE(SUM(CASE WHEN t.category.type = 'INCOME' THEN t.amount ELSE -t.amount END), 0)
        FROM Transaction t
        WHERE t.user = :user
          AND t.date >= :since
    """)
    BigDecimal calculateNetSavingsSince(@Param("user") User user, @Param("since") LocalDate since);

    /** Check if any transaction references a given category. */
    boolean existsByCategory(Category category);
}
