package com.financeai.controller;

import com.financeai.dto.response.DashboardResponse;
import com.financeai.dto.response.MonthlySpendingResponse;
import com.financeai.dto.response.TransactionResponse;
import com.financeai.entity.User;
import com.financeai.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardResponse> getSummary(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();
        return ResponseEntity.ok(dashboardService.getSummary(user.getId(), m, y));
    }

    @GetMapping("/monthly-spending")
    public ResponseEntity<List<MonthlySpendingResponse>> getMonthlySpending(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "6") int months) {
        return ResponseEntity.ok(dashboardService.getMonthlySpending(user.getId(), months));
    }

    @GetMapping("/recent-transactions")
    public ResponseEntity<List<TransactionResponse>> getRecentTransactions(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(dashboardService.getRecentTransactions(user.getId(), limit));
    }
}
