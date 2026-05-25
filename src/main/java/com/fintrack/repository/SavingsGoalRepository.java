package com.fintrack.repository;

import com.fintrack.entity.SavingsGoal;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for SavingsGoal entities.
 */
@Repository
public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {

    List<SavingsGoal> findByUser(User user);

    Optional<SavingsGoal> findByIdAndUser(Long id, User user);
}
