package com.financeai.controller;

import com.financeai.dto.request.IncomeRequest;
import com.financeai.dto.response.IncomeResponse;
import com.financeai.dto.response.PageResponse;
import com.financeai.entity.User;
import com.financeai.service.IncomeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    public IncomeController(IncomeService incomeService) {
        this.incomeService = incomeService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<IncomeResponse>> getIncomes(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by("date").ascending() : Sort.by("date").descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        return ResponseEntity.ok(incomeService.getIncomes(user.getId(), source, startDate, endDate, pageable));
    }

    @PostMapping
    public ResponseEntity<IncomeResponse> createIncome(@AuthenticationPrincipal User user,
                                                        @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(incomeService.createIncome(user.getId(), request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeResponse> getIncome(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeResponse> updateIncome(@AuthenticationPrincipal User user,
                                                        @PathVariable Long id,
                                                        @Valid @RequestBody IncomeRequest request) {
        return ResponseEntity.ok(incomeService.updateIncome(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@AuthenticationPrincipal User user, @PathVariable Long id) {
        incomeService.deleteIncome(user.getId(), id);
        return ResponseEntity.noContent().build();
    }
}
