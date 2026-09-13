package com.financeai.service;

import com.financeai.dto.response.BudgetResponse;
import com.financeai.entity.Budget;
import com.financeai.entity.User;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock private BudgetRepository budgetRepository;
    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private BudgetService budgetService;

    private User user;
    private Budget budget;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test").email("test@example.com").password("pw").build();
        budget = Budget.builder().id(1L).category("FOOD_DINING")
                .limitAmount(new BigDecimal("400.00")).month(9).year(2026).user(user).build();
    }

    @Test
    void getBudgets_computesSpentAmountCorrectly() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 9, 2026)).thenReturn(List.of(budget));
        when(expenseRepository.sumByUserCategoryAndDateRange(eq(1L), eq("FOOD_DINING"),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("250.00"));

        List<BudgetResponse> result = budgetService.getBudgets(1L, 9, 2026);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).spentAmount()).isEqualByComparingTo("250.00");
        assertThat(result.get(0).isExceeded()).isFalse();
    }

    @Test
    void getBudgets_marksExceeded_whenSpentExceedsLimit() {
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 9, 2026)).thenReturn(List.of(budget));
        when(expenseRepository.sumByUserCategoryAndDateRange(eq(1L), eq("FOOD_DINING"),
                any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("450.00"));

        List<BudgetResponse> result = budgetService.getBudgets(1L, 9, 2026);
        assertThat(result.get(0).isExceeded()).isTrue();
    }

    @Test
    void createBudget_duplicateCategory_throwsIllegalArgument() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(budgetRepository.findByUserIdAndCategoryAndMonthAndYear(1L, "FOOD_DINING", 9, 2026))
                .thenReturn(Optional.of(budget));

        com.financeai.dto.request.BudgetRequest request = new com.financeai.dto.request.BudgetRequest();
        request.setCategory("FOOD_DINING");
        request.setLimitAmount(new BigDecimal("500.00"));
        request.setMonth(9);
        request.setYear(2026);

        assertThatThrownBy(() -> budgetService.createBudget(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Budget already exists");
    }
}
