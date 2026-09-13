package com.financeai.service;

import com.financeai.dto.request.BudgetRequest;
import com.financeai.dto.response.BudgetResponse;
import com.financeai.entity.Budget;
import com.financeai.entity.User;
import com.financeai.exception.ResourceNotFoundException;
import com.financeai.repository.BudgetRepository;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         ExpenseRepository expenseRepository,
                         UserRepository userRepository) {
        this.budgetRepository = budgetRepository;
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public List<BudgetResponse> getBudgets(Long userId, Integer month, Integer year) {
        return budgetRepository.findByUserIdAndMonthAndYear(userId, month, year)
                .stream().map(b -> toResponse(b, userId)).toList();
    }

    @Transactional
    public BudgetResponse createBudget(Long userId, BudgetRequest request) {
        User user = findUser(userId);
        budgetRepository.findByUserIdAndCategoryAndMonthAndYear(
                userId, request.getCategory(), request.getMonth(), request.getYear())
                .ifPresent(b -> { throw new IllegalArgumentException(
                        "Budget already exists for category " + request.getCategory() +
                        " in " + request.getMonth() + "/" + request.getYear()); });

        Budget budget = Budget.builder()
                .category(request.getCategory())
                .limitAmount(request.getLimitAmount())
                .month(request.getMonth())
                .year(request.getYear())
                .user(user)
                .build();
        return toResponse(budgetRepository.save(budget), userId);
    }

    public BudgetResponse getBudgetById(Long userId, Long budgetId) {
        return toResponse(findBudgetByIdAndUser(budgetId, userId), userId);
    }

    @Transactional
    public BudgetResponse updateBudget(Long userId, Long budgetId, BudgetRequest request) {
        Budget budget = findBudgetByIdAndUser(budgetId, userId);
        budget.setCategory(request.getCategory());
        budget.setLimitAmount(request.getLimitAmount());
        budget.setMonth(request.getMonth());
        budget.setYear(request.getYear());
        return toResponse(budgetRepository.save(budget), userId);
    }

    @Transactional
    public void deleteBudget(Long userId, Long budgetId) {
        budgetRepository.delete(findBudgetByIdAndUser(budgetId, userId));
    }

    public BudgetResponse toResponse(Budget budget, Long userId) {
        YearMonth ym = YearMonth.of(budget.getYear(), budget.getMonth());
        LocalDate start = ym.atDay(1);
        LocalDate end   = ym.atEndOfMonth();

        BigDecimal spent = expenseRepository.sumByUserCategoryAndDateRange(
                userId, budget.getCategory(), start, end);
        if (spent == null) spent = BigDecimal.ZERO;

        double util = budget.getLimitAmount().compareTo(BigDecimal.ZERO) > 0
                ? spent.divide(budget.getLimitAmount(), 4, RoundingMode.HALF_UP)
                       .multiply(BigDecimal.valueOf(100)).doubleValue()
                : 0.0;

        return new BudgetResponse(budget.getId(), budget.getCategory(), budget.getLimitAmount(),
                budget.getMonth(), budget.getYear(), spent,
                Math.round(util * 100.0) / 100.0,
                spent.compareTo(budget.getLimitAmount()) > 0);
    }

    private Budget findBudgetByIdAndUser(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget", budgetId));
        if (!budget.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Budget", budgetId);
        }
        return budget;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }
}
