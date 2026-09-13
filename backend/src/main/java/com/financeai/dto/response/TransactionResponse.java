package com.financeai.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        Long id,
        String type,  // "EXPENSE" or "INCOME"
        String description,
        BigDecimal amount,
        String category,  // category for expense, source for income
        LocalDate date
) {}
