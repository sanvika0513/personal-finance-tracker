package com.financetracker.service;

import com.financetracker.dto.BudgetStatusDto;
import com.financetracker.dto.DashboardSummaryDto;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final BudgetService budgetService;

    public DashboardSummaryDto getSummary(String username, int month, int year) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        List<Transaction> incomeTx = transactionRepository.findByUserAndTypeAndDateBetween(
                user, Transaction.TransactionType.INCOME, start, end);
        List<Transaction> expenseTx = transactionRepository.findByUserAndTypeAndDateBetween(
                user, Transaction.TransactionType.EXPENSE, start, end);

        BigDecimal totalIncome = incomeTx.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpense = expenseTx.stream().map(Transaction::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, BigDecimal> expensesByCategory = expenseTx.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        Map<String, BigDecimal> incomeByCategory = incomeTx.stream()
                .collect(Collectors.groupingBy(Transaction::getCategory,
                        Collectors.reducing(BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

        List<BudgetStatusDto> budgetStatuses = budgetService.getBudgetStatus(username, month, year);

        return DashboardSummaryDto.builder()
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .balance(totalIncome.subtract(totalExpense))
                .expensesByCategory(expensesByCategory)
                .incomeByCategory(incomeByCategory)
                .budgetStatuses(budgetStatuses)
                .build();
    }
}
