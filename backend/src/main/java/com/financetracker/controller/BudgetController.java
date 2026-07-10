package com.financetracker.controller;

import com.financetracker.dto.BudgetDto;
import com.financetracker.dto.BudgetStatusDto;
import com.financetracker.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService budgetService;

    @GetMapping
    public ResponseEntity<List<BudgetDto>> getAll(Authentication authentication) {
        return ResponseEntity.ok(budgetService.getAllForUser(authentication.getName()));
    }

    @GetMapping("/status")
    public ResponseEntity<List<BudgetStatusDto>> getStatus(
            Authentication authentication,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {

        LocalDate now = LocalDate.now();
        int m = month != null ? month : now.getMonthValue();
        int y = year != null ? year : now.getYear();

        return ResponseEntity.ok(budgetService.getBudgetStatus(authentication.getName(), m, y));
    }

    @PostMapping
    public ResponseEntity<BudgetDto> create(Authentication authentication, @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(authentication.getName(), dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BudgetDto> update(Authentication authentication,
                                             @PathVariable Long id,
                                             @Valid @RequestBody BudgetDto dto) {
        return ResponseEntity.ok(budgetService.update(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        budgetService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
