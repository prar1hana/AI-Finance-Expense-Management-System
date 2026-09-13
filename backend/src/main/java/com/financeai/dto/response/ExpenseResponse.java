package com.financeai.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ExpenseResponse(
        Long id,
        String description,
        BigDecimal amount,
        String category,
        LocalDate date,
        String notes,
        LocalDateTime createdAt
) {}
