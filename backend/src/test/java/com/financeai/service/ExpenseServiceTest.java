package com.financeai.service;

import com.financeai.dto.request.ExpenseRequest;
import com.financeai.dto.response.ExpenseResponse;
import com.financeai.dto.response.PageResponse;
import com.financeai.entity.Expense;
import com.financeai.entity.User;
import com.financeai.exception.ResourceNotFoundException;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private UserRepository userRepository;
    @InjectMocks private ExpenseService expenseService;

    private User user;
    private Expense expense;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).name("Test User")
                .email("test@example.com").password("encodedPassword").build();
        expense = Expense.builder().id(1L).description("Lunch")
                .amount(new BigDecimal("12.50")).category("FOOD_DINING")
                .date(LocalDate.now()).user(user).build();
        expense.setCreatedAt(LocalDateTime.now());
        expense.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void createExpense_valid_persistsAndReturnsResponse() {
        ExpenseRequest request = new ExpenseRequest();
        request.setDescription("Lunch");
        request.setAmount(new BigDecimal("12.50"));
        request.setCategory("FOOD_DINING");
        request.setDate(LocalDate.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        ExpenseResponse response = expenseService.createExpense(1L, request);
        assertThat(response.description()).isEqualTo("Lunch");
        assertThat(response.amount()).isEqualByComparingTo("12.50");
        verify(expenseRepository).save(any(Expense.class));
    }

    @Test
    void createExpense_userNotFound_throwsResourceNotFoundException() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> expenseService.createExpense(99L, new ExpenseRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getExpenses_withFilters_returnsPageResponse() {
        Page<Expense> page = new PageImpl<>(List.of(expense));
        when(expenseRepository.findByFilters(eq(1L), any(), any(), any(), any(Pageable.class)))
                .thenReturn(page);
        PageResponse<ExpenseResponse> result = expenseService.getExpenses(
                1L, null, null, null, PageRequest.of(0, 10));
        assertThat(result.content()).hasSize(1);
    }

    @Test
    void deleteExpense_notOwnedByUser_throwsResourceNotFoundException() {
        User other = User.builder().id(2L).name("Other").email("o@o.com").password("pw").build();
        Expense otherExpense = Expense.builder().id(5L).description("Other")
                .amount(BigDecimal.TEN).category("OTHER").date(LocalDate.now()).user(other).build();
        when(expenseRepository.findById(5L)).thenReturn(Optional.of(otherExpense));
        assertThatThrownBy(() -> expenseService.deleteExpense(1L, 5L))
                .isInstanceOf(ResourceNotFoundException.class);
        verify(expenseRepository, never()).delete(any());
    }

    @Test
    void deleteExpense_validOwner_deletesExpense() {
        when(expenseRepository.findById(1L)).thenReturn(Optional.of(expense));
        doNothing().when(expenseRepository).delete(expense);
        expenseService.deleteExpense(1L, 1L);
        verify(expenseRepository).delete(expense);
    }
}
