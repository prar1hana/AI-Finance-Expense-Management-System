package com.financeai.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record IncomeResponse(
        Long id,
        String description,
        BigDecimal amount,
        String source,
        LocalDate date,
        String notes,
        LocalDateTime createdAt
) {}
