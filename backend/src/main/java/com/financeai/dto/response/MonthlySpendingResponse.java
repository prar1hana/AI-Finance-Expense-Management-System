package com.financeai.dto.response;

import java.math.BigDecimal;

public record MonthlySpendingResponse(String month, int year, int monthNumber, BigDecimal amount) {}
