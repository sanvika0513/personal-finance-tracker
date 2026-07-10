package com.financetracker.controller;

import com.financetracker.dto.TransactionDto;
import com.financetracker.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<List<TransactionDto>> getAll(
            Authentication authentication,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

        String username = authentication.getName();
        if (start != null && end != null) {
            return ResponseEntity.ok(transactionService.getForUserBetween(username, start, end));
        }
        return ResponseEntity.ok(transactionService.getAllForUser(username));
    }

    @PostMapping
    public ResponseEntity<TransactionDto> create(Authentication authentication,
                                                  @Valid @RequestBody TransactionDto dto) {
        TransactionDto created = transactionService.create(authentication.getName(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TransactionDto> update(Authentication authentication,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody TransactionDto dto) {
        return ResponseEntity.ok(transactionService.update(authentication.getName(), id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(Authentication authentication, @PathVariable Long id) {
        transactionService.delete(authentication.getName(), id);
        return ResponseEntity.noContent().build();
    }
}
