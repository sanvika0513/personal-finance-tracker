package com.financetracker.service;

import com.financetracker.dto.BudgetDto;
import com.financetracker.dto.BudgetStatusDto;
import com.financetracker.entity.Budget;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.exception.AccessDeniedCustomException;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.BudgetRepository;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public BudgetDto create(String username, BudgetDto dto) {
        User user = getUser(username);

        budgetRepository.findByUserAndCategoryAndMonthAndYear(user, dto.getCategory(), dto.getMonth(), dto.getYear())
                .ifPresent(b -> { throw new DuplicateResourceException(
                        "Budget for category '" + dto.getCategory() + "' already exists for " + dto.getMonth() + "/" + dto.getYear()); });

        Budget budget = Budget.builder()
                .user(user)
                .category(dto.getCategory())
                .monthlyLimit(dto.getMonthlyLimit())
                .month(dto.getMonth())
                .year(dto.getYear())
                .build();

        return toDto(budgetRepository.save(budget));
    }

    public BudgetDto update(String username, Long id, BudgetDto dto) {
        User user = getUser(username);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("You do not have permission to modify this budget");
        }

        budget.setCategory(dto.getCategory());
        budget.setMonthlyLimit(dto.getMonthlyLimit());
        budget.setMonth(dto.getMonth());
        budget.setYear(dto.getYear());

        return toDto(budgetRepository.save(budget));
    }

    public void delete(String username, Long id) {
        User user = getUser(username);
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));

        if (!budget.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("You do not have permission to delete this budget");
        }

        budgetRepository.delete(budget);
    }

    public List<BudgetDto> getAllForUser(String username) {
        User user = getUser(username);
        return budgetRepository.findByUser(user).stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<BudgetStatusDto> getBudgetStatus(String username, int month, int year) {
        User user = getUser(username);
        List<Budget> budgets = budgetRepository.findByUserAndMonthAndYear(user, month, year);

        YearMonth ym = YearMonth.of(year, month);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();

        return budgets.stream().map(b -> {
            List<Transaction> spentTx = transactionRepository.findByUserAndCategoryAndTypeAndDateBetween(
                    user, b.getCategory(), Transaction.TransactionType.EXPENSE, start, end);

            BigDecimal spent = spentTx.stream()
                    .map(Transaction::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal remaining = b.getMonthlyLimit().subtract(spent);
            double percentUsed = b.getMonthlyLimit().compareTo(BigDecimal.ZERO) > 0
                    ? spent.divide(b.getMonthlyLimit(), 4, RoundingMode.HALF_UP).doubleValue() * 100
                    : 0.0;

            return BudgetStatusDto.builder()
                    .category(b.getCategory())
                    .monthlyLimit(b.getMonthlyLimit())
                    .spent(spent)
                    .remaining(remaining)
                    .overBudget(spent.compareTo(b.getMonthlyLimit()) > 0)
                    .percentUsed(percentUsed)
                    .build();
        }).collect(Collectors.toList());
    }

    private BudgetDto toDto(Budget b) {
        return new BudgetDto(b.getId(), b.getCategory(), b.getMonthlyLimit(), b.getMonth(), b.getYear());
    }
}
