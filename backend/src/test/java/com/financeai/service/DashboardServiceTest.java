package com.financeai.service;

import com.financeai.dto.response.DashboardResponse;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DashboardServiceTest {

    @Mock private ExpenseRepository expenseRepository;
    @Mock private IncomeRepository incomeRepository;
    @Mock private BudgetRepository budgetRepository;
    @Mock private BudgetService budgetService;
    @InjectMocks private DashboardService dashboardService;

    @Test
    void getSummary_netBalanceEqualsIncomeMinusExpenses() {
        when(incomeRepository.sumByUserAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("4200.00"));
        when(expenseRepository.sumByUserAndDateRange(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new BigDecimal("1800.00"));
        when(expenseRepository.findCategoryTotals(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(budgetRepository.findByUserIdAndMonthAndYear(1L, 9, 2026)).thenReturn(List.of());

        DashboardResponse response = dashboardService.getSummary(1L, 9, 2026);

        assertThat(response.totalIncome()).isEqualByComparingTo("4200.00");
        assertThat(response.totalExpenses()).isEqualByComparingTo("1800.00");
        assertThat(response.netBalance()).isEqualByComparingTo("2400.00");
    }

    @Test
    void getSummary_nullValues_treatedAsZero() {
        when(incomeRepository.sumByUserAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(expenseRepository.sumByUserAndDateRange(anyLong(), any(), any())).thenReturn(null);
        when(expenseRepository.findCategoryTotals(anyLong(), any(), any())).thenReturn(List.of());
        when(budgetRepository.findByUserIdAndMonthAndYear(anyLong(), anyInt(), anyInt()))
                .thenReturn(List.of());

        DashboardResponse response = dashboardService.getSummary(1L, 9, 2026);
        assertThat(response.netBalance()).isEqualByComparingTo("0.00");
    }

    @Test
    void getMonthlySpending_returnsCorrectNumberOfMonths() {
        when(expenseRepository.sumByUserAndDateRange(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(BigDecimal.ZERO);

        var result = dashboardService.getMonthlySpending(1L, 6);
        assertThat(result).hasSize(6);
    }
}
