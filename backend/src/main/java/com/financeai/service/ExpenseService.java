package com.financeai.service;

import com.financeai.dto.request.ExpenseRequest;
import com.financeai.dto.response.DashboardResponse;
import com.financeai.dto.response.ExpenseResponse;
import com.financeai.dto.response.PageResponse;
import com.financeai.entity.Expense;
import com.financeai.entity.User;
import com.financeai.exception.ResourceNotFoundException;
import com.financeai.repository.ExpenseRepository;
import com.financeai.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

@Service
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final UserRepository userRepository;

    public ExpenseService(ExpenseRepository expenseRepository, UserRepository userRepository) {
        this.expenseRepository = expenseRepository;
        this.userRepository = userRepository;
    }

    public PageResponse<ExpenseResponse> getExpenses(Long userId, String category,
                                                      LocalDate startDate, LocalDate endDate,
                                                      Pageable pageable) {
        Page<Expense> page = expenseRepository.findByFilters(userId, category, startDate, endDate, pageable);
        return toPageResponse(page);
    }

    @Transactional
    public ExpenseResponse createExpense(Long userId, ExpenseRequest request) {
        User user = findUser(userId);
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .category(request.getCategory())
                .date(request.getDate())
                .notes(request.getNotes())
                .user(user)
                .build();
        return toResponse(expenseRepository.save(expense));
    }

    public ExpenseResponse getExpenseById(Long userId, Long expenseId) {
        return toResponse(findExpenseByIdAndUser(expenseId, userId));
    }

    @Transactional
    public ExpenseResponse updateExpense(Long userId, Long expenseId, ExpenseRequest request) {
        Expense expense = findExpenseByIdAndUser(expenseId, userId);
        expense.setDescription(request.getDescription());
        expense.setAmount(request.getAmount());
        expense.setCategory(request.getCategory());
        expense.setDate(request.getDate());
        expense.setNotes(request.getNotes());
        return toResponse(expenseRepository.save(expense));
    }

    @Transactional
    public void deleteExpense(Long userId, Long expenseId) {
        expenseRepository.delete(findExpenseByIdAndUser(expenseId, userId));
    }

    public List<DashboardResponse.CategorySummary> getCategoryTotals(Long userId, int month, int year) {
        YearMonth ym = YearMonth.of(year, month);
        List<Object[]> rows = expenseRepository.findCategoryTotals(userId, ym.atDay(1), ym.atEndOfMonth());
        BigDecimal total = rows.stream().map(r -> (BigDecimal) r[1]).reduce(BigDecimal.ZERO, BigDecimal::add);
        return rows.stream().map(row -> {
            String category = (String) row[0];
            BigDecimal amount = (BigDecimal) row[1];
            double pct = total.compareTo(BigDecimal.ZERO) > 0
                    ? amount.divide(total, 4, RoundingMode.HALF_UP)
                             .multiply(BigDecimal.valueOf(100)).doubleValue()
                    : 0.0;
            return new DashboardResponse.CategorySummary(category, amount, Math.round(pct * 100.0) / 100.0);
        }).toList();
    }

    private Expense findExpenseByIdAndUser(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ResourceNotFoundException("Expense", expenseId));
        if (!expense.getUser().getId().equals(userId)) {
            throw new ResourceNotFoundException("Expense", expenseId);
        }
        return expense;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    public ExpenseResponse toResponse(Expense e) {
        return new ExpenseResponse(e.getId(), e.getDescription(), e.getAmount(),
                e.getCategory(), e.getDate(), e.getNotes(), e.getCreatedAt());
    }

    private PageResponse<ExpenseResponse> toPageResponse(Page<Expense> page) {
        return new PageResponse<>(page.getContent().stream().map(this::toResponse).toList(),
                page.getNumber(), page.getSize(), page.getTotalElements(),
                page.getTotalPages(), page.isLast());
    }
}
