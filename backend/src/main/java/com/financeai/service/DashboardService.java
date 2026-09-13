package com.financeai.service;

import com.financeai.dto.response.*;
import com.financeai.entity.Expense;
import com.financeai.entity.Income;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.IncomeRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Month;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private final ExpenseRepository expenseRepository;
    private final IncomeRepository incomeRepository;
    private final BudgetRepository budgetRepository;
    private final BudgetService budgetService;

    public DashboardService(ExpenseRepository expenseRepository,
                             IncomeRepository incomeRepository,
                             BudgetRepository budgetRepository,
                             BudgetService budgetService) {
        this.expenseRepository = expenseRepository;
        this.incomeRepository = incomeRepository;
        this.budgetRepository = budgetRepository;
        this.budgetService = budgetService;
    }

    public DashboardResponse getSummary(Long userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        BigDecimal totalIncome   = incomeRepository.sumByUserAndDateRange(userId, start, end);
        BigDecimal totalExpenses = expenseRepository.sumByUserAndDateRange(userId, start, end);
        if (totalIncome == null)   totalIncome   = BigDecimal.ZERO;
        if (totalExpenses == null) totalExpenses = BigDecimal.ZERO;

        List<Object[]> categoryTotals = expenseRepository.findCategoryTotals(userId, start, end);
        List<DashboardResponse.CategorySummary> breakdown = buildBreakdown(categoryTotals, totalExpenses);

        List<BudgetResponse> budgetAlerts = budgetRepository
                .findByUserIdAndMonthAndYear(userId, month, year).stream()
                .map(b -> budgetService.toResponse(b, userId))
                .filter(b -> b.utilizationPercent() >= 80)
                .collect(Collectors.toList());

        return new DashboardResponse(totalIncome, totalExpenses,
                totalIncome.subtract(totalExpenses), breakdown, budgetAlerts, month, year);
    }

    public List<MonthlySpendingResponse> getMonthlySpending(Long userId, int months) {
        List<MonthlySpendingResponse> result = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = months - 1; i >= 0; i--) {
            YearMonth ym = current.minusMonths(i);
            LocalDate start = ym.atDay(1);
            LocalDate end   = ym.atEndOfMonth();
            BigDecimal amount = expenseRepository.sumByUserAndDateRange(userId, start, end);
            if (amount == null) amount = BigDecimal.ZERO;
            String monthName = Month.of(ym.getMonthValue()).getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            result.add(new MonthlySpendingResponse(monthName, ym.getYear(), ym.getMonthValue(), amount));
        }
        return result;
    }

    public List<TransactionResponse> getRecentTransactions(Long userId, int limit) {
        List<Expense> expenses = expenseRepository.findRecentByUserId(userId, PageRequest.of(0, limit));
        List<Income>  incomes  = incomeRepository.findRecentByUserId(userId, PageRequest.of(0, limit));
        List<TransactionResponse> transactions = new ArrayList<>();
        expenses.forEach(e -> transactions.add(new TransactionResponse(
                e.getId(), "EXPENSE", e.getDescription(), e.getAmount(), e.getCategory(), e.getDate())));
        incomes.forEach(i -> transactions.add(new TransactionResponse(
                i.getId(), "INCOME", i.getDescription(), i.getAmount(), i.getSource(), i.getDate())));
        transactions.sort(Comparator.comparing(TransactionResponse::date).reversed());
        return transactions.stream().limit(limit).collect(Collectors.toList());
    }

    private List<DashboardResponse.CategorySummary> buildBreakdown(List<Object[]> rows, BigDecimal total) {
        return rows.stream().map(row -> {
            String cat     = (String) row[0];
            BigDecimal amt = (BigDecimal) row[1];
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amt.divide(total, 4, RoundingMode.HALF_UP)
                         .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return new DashboardResponse.CategorySummary(cat, amt, Math.round(pct * 100.0) / 100.0);
        }).collect(Collectors.toList());
    }
}
