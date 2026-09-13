package com.financeai.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record DashboardResponse(
        BigDecimal totalIncome,
        BigDecimal totalExpenses,
        BigDecimal netBalance,
        List<CategorySummary> categoryBreakdown,
        List<BudgetResponse> budgetAlerts,
        int month,
        int year
) {
    public record CategorySummary(String category, BigDecimal amount, double percentage) {}
}
