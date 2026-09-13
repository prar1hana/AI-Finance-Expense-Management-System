package com.financeai.controller;

import com.financeai.dto.request.BudgetRequest;
import com.financeai.dto.response.BudgetResponse;
import com.financeai.entity.User;
import com.financeai.service.BudgetService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> getBudgets(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        LocalDate now = LocalDate.now();
        int m = (month != null) ? month : now.getMonthValue();
        int y = (year != null) ? year : now.getYear();
        return ResponseEntity.ok(budgetService.getBudgets(user.getId(), m, y));
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> createBudget(@AuthenticationPrincipal User user,
                                                        @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.createBudget(user.getId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> getBudget(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getBudgetById(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetResponse> updateBudget(@AuthenticationPrincipal User user,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody BudgetRequest request) {
        return ResponseEntity.ok(budgetService.updateBudget(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBudget(@AuthenticationPrincipal User user, @PathVariable Long id) {
        budgetService.deleteBudget(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
