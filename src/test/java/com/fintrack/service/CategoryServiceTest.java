package com.fintrack.service;

import com.fintrack.dto.request.CategoryRequest;
import com.fintrack.dto.response.Responses.CategoryListResponse;
import com.fintrack.dto.response.Responses.CategoryResponse;
import com.fintrack.dto.response.Responses.MessageResponse;
import com.fintrack.entity.Category;
import com.fintrack.entity.TransactionType;
import com.fintrack.entity.User;
import com.fintrack.exception.BadRequestException;
import com.fintrack.exception.ConflictException;
import com.fintrack.exception.ForbiddenException;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryRepository categoryRepository;
    @Mock private TransactionRepository transactionRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private CategoryService categoryService;

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
    void getAll_returnsMergedList() {
        Category salary = Category.builder().name("Salary").type(TransactionType.INCOME).isCustom(false).build();
        Category custom = Category.builder().name("Freelance").type(TransactionType.INCOME).isCustom(true).user(user).build();

        when(categoryRepository.findByIsCustomFalse()).thenReturn(List.of(salary));
        when(categoryRepository.findByUserAndIsCustomTrue(user)).thenReturn(List.of(custom));

        CategoryListResponse response = categoryService.getAll();
        assertThat(response.getCategories()).hasSize(2);
    }

    @Test
    void create_success() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Freelance");
        req.setType(TransactionType.INCOME);

        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(false);
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());

        Category saved = Category.builder().name("Freelance").type(TransactionType.INCOME).isCustom(true).build();
        when(categoryRepository.save(any())).thenReturn(saved);

        CategoryResponse response = categoryService.create(req);
        assertThat(response.getName()).isEqualTo("Freelance");
        assertThat(response.isCustom()).isTrue();
    }

    @Test
    void create_duplicateName_throwsConflict() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Freelance");
        req.setType(TransactionType.INCOME);
        when(categoryRepository.existsByNameAndUser("Freelance", user)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void create_shadowsDefaultName_throwsConflict() {
        CategoryRequest req = new CategoryRequest();
        req.setName("Salary");
        req.setType(TransactionType.INCOME);
        when(categoryRepository.existsByNameAndUser("Salary", user)).thenReturn(false);
        when(categoryRepository.findByNameAndIsCustomFalse("Salary"))
                .thenReturn(Optional.of(Category.builder().name("Salary").build()));

        assertThatThrownBy(() -> categoryService.create(req))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void delete_success() {
        Category custom = Category.builder().id(1L).name("Freelance").isCustom(true).user(user).build();
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(custom));
        when(transactionRepository.existsByCategory(custom)).thenReturn(false);

        MessageResponse response = categoryService.delete("Freelance");
        assertThat(response.getMessage()).contains("deleted");
        verify(categoryRepository).delete(custom);
    }

    @Test
    void delete_defaultCategory_throwsForbidden() {
        when(categoryRepository.findByNameAndIsCustomFalse("Salary"))
                .thenReturn(Optional.of(Category.builder().name("Salary").build()));
        assertThatThrownBy(() -> categoryService.delete("Salary"))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void delete_categoryInUse_throwsBadRequest() {
        Category custom = Category.builder().id(1L).name("Freelance").isCustom(true).user(user).build();
        when(categoryRepository.findByNameAndIsCustomFalse("Freelance")).thenReturn(Optional.empty());
        when(categoryRepository.findByNameAndUser("Freelance", user)).thenReturn(Optional.of(custom));
        when(transactionRepository.existsByCategory(custom)).thenReturn(true);

        assertThatThrownBy(() -> categoryService.delete("Freelance"))
                .isInstanceOf(BadRequestException.class);
    }
}
