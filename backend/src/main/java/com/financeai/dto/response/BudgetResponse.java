package com.financeai.dto.response;

import java.math.BigDecimal;

public record BudgetResponse(
        Long id,
        String category,
        BigDecimal limitAmount,
        Integer month,
        Integer year,
        BigDecimal spentAmount,
        double utilizationPercent,
        boolean isExceeded
) {}
