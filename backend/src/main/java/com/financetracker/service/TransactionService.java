package com.financetracker.service;

import com.financetracker.dto.TransactionDto;
import com.financetracker.entity.Transaction;
import com.financetracker.entity.User;
import com.financetracker.exception.AccessDeniedCustomException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.repository.TransactionRepository;
import com.financetracker.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
    }

    public List<TransactionDto> getAllForUser(String username) {
        User user = getUser(username);
        return transactionRepository.findByUserOrderByDateDesc(user)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public List<TransactionDto> getForUserBetween(String username, LocalDate start, LocalDate end) {
        User user = getUser(username);
        return transactionRepository.findByUserAndDateBetweenOrderByDateDesc(user, start, end)
                .stream().map(this::toDto).collect(Collectors.toList());
    }

    public TransactionDto create(String username, TransactionDto dto) {
        User user = getUser(username);
        Transaction transaction = Transaction.builder()
                .user(user)
                .type(dto.getType())
                .category(dto.getCategory())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .description(dto.getDescription())
                .build();
        return toDto(transactionRepository.save(transaction));
    }

    public TransactionDto update(String username, Long id, TransactionDto dto) {
        User user = getUser(username);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("You do not have permission to modify this transaction");
        }

        transaction.setType(dto.getType());
        transaction.setCategory(dto.getCategory());
        transaction.setAmount(dto.getAmount());
        transaction.setDate(dto.getDate());
        transaction.setDescription(dto.getDescription());

        return toDto(transactionRepository.save(transaction));
    }

    public void delete(String username, Long id) {
        User user = getUser(username);
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found: " + id));

        if (!transaction.getUser().getId().equals(user.getId())) {
            throw new AccessDeniedCustomException("You do not have permission to delete this transaction");
        }

        transactionRepository.delete(transaction);
    }

    private TransactionDto toDto(Transaction t) {
        return new TransactionDto(t.getId(), t.getType(), t.getCategory(), t.getAmount(), t.getDate(), t.getDescription());
    }
}
