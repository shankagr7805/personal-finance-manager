package com.fintrack.repository;

import com.fintrack.entity.Category;
import com.fintrack.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Data access layer for Category entities.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /** Fetch default (system) categories that are available to all users. */
    List<Category> findByIsCustomFalse();

    /** Fetch custom categories belonging to a specific user. */
    List<Category> findByUserAndIsCustomTrue(User user);

    /** Find a custom category by name for a specific user. */
    Optional<Category> findByNameAndUser(String name, User user);

    /** Find a default category by name. */
    Optional<Category> findByNameAndIsCustomFalse(String name);

    /**
     * Check whether a category name already exists for a user
     * (to enforce uniqueness per user for custom categories).
     */
    @Query("SELECT COUNT(c) > 0 FROM Category c WHERE c.name = :name AND c.user = :user AND c.isCustom = true")
    boolean existsByNameAndUser(@Param("name") String name, @Param("user") User user);

    /** Find category by name for a given user — checks both default and custom. */
    @Query("SELECT c FROM Category c WHERE c.name = :name AND (c.isCustom = false OR c.user = :user)")
    Optional<Category> findAccessibleByName(@Param("name") String name, @Param("user") User user);
}
