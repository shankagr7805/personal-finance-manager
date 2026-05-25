package com.fintrack.config;

import com.fintrack.entity.Category;
import com.fintrack.entity.TransactionType;
import com.fintrack.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Seeds the database with system-defined default categories on startup.
 * These cannot be modified or deleted by users.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        if (categoryRepository.findByIsCustomFalse().isEmpty()) {
            List<Category> defaults = List.of(
                buildDefault("Salary", TransactionType.INCOME),
                buildDefault("Food", TransactionType.EXPENSE),
                buildDefault("Rent", TransactionType.EXPENSE),
                buildDefault("Transportation", TransactionType.EXPENSE),
                buildDefault("Entertainment", TransactionType.EXPENSE),
                buildDefault("Healthcare", TransactionType.EXPENSE),
                buildDefault("Utilities", TransactionType.EXPENSE)
            );
            categoryRepository.saveAll(defaults);
            log.info("Default categories seeded successfully.");
        }
    }

    private Category buildDefault(String name, TransactionType type) {
        return Category.builder()
                .name(name)
                .type(type)
                .isCustom(false)
                .user(null)
                .build();
    }
}
