package com.fintech.ledger.service;

import com.fintech.ledger.domain.EventType;
import com.fintech.ledger.domain.LedgerEvent;
import com.fintech.ledger.dto.AccountResponse;
import com.fintech.ledger.dto.CreateAccountRequest;
import com.fintech.ledger.dto.DepositRequest;
import com.fintech.ledger.dto.WithdrawRequest;
import com.fintech.ledger.exception.AccountNotFoundException;
import com.fintech.ledger.exception.InsufficientFundsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {
    
    private final EventStoreService eventStoreService;
    private final ProjectionService projectionService;
    
    @Transactional
    public AccountResponse createAccount(CreateAccountRequest request) {
        UUID accountId = UUID.randomUUID();
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("accountHolderName", request.getAccountHolderName());
        metadata.put("createdAt", Instant.now().toString());
        
        // Append ACCOUNT_OPENED event
        LedgerEvent event = eventStoreService.appendEvent(
            accountId,
            EventType.ACCOUNT_OPENED,
            BigDecimal.ZERO,
            request.getCurrency(),
            metadata
        );
        
        // Project to read model
        projectionService.projectEvent(event);
        
        log.info("Account created: {}", accountId);
        
        return AccountResponse.builder()
            .accountId(accountId)
            .balance(BigDecimal.ZERO)
            .currency(request.getCurrency())
            .createdAt(event.getOccurredAt())
            .eventCount(1L)
            .build();
    }
    
    @Transactional
    public void deposit(DepositRequest request) {
        // Verify account exists
        if (projectionService.getAccountBalance(request.getAccountId()) == null) {
            throw new AccountNotFoundException("Account not found: " + request.getAccountId());
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", request.getDescription());
        
        // Append MONEY_DEPOSITED event
        LedgerEvent event = eventStoreService.appendEvent(
            request.getAccountId(),
            EventType.MONEY_DEPOSITED,
            request.getAmount(),
            "USD", // Get from account
            metadata
        );
        
        // Project to read model
        projectionService.projectEvent(event);
        
        log.info("Deposited {} to account {}", request.getAmount(), request.getAccountId());
    }
    
    @Transactional
    public void withdraw(WithdrawRequest request) {
        // Verify account exists
        BigDecimal currentBalance = projectionService.getBalance(request.getAccountId());
        if (currentBalance == null) {
            throw new AccountNotFoundException("Account not found: " + request.getAccountId());
        }
        
        // Check sufficient funds
        if (currentBalance.compareTo(request.getAmount()) < 0) {
            throw new InsufficientFundsException(
                String.format("Insufficient funds. Balance: %s, Requested: %s", 
                    currentBalance, request.getAmount())
            );
        }
        
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("description", request.getDescription());
        
        // Append MONEY_WITHDRAWN event
        LedgerEvent event = eventStoreService.appendEvent(
            request.getAccountId(),
            EventType.MONEY_WITHDRAWN,
            request.getAmount(),
            "USD",
            metadata
        );
        
        // Project to read model
        projectionService.projectEvent(event);
        
        log.info("Withdrew {} from account {}", request.getAmount(), request.getAccountId());
    }
    
    public AccountResponse getAccount(UUID accountId) {
        var balance = projectionService.getAccountBalance(accountId);
        if (balance == null) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        
        var events = eventStoreService.getAccountEvents(accountId);
        
        return AccountResponse.builder()
            .accountId(accountId)
            .balance(balance.getBalance())
            .currency(balance.getCurrency())
            .createdAt(events.isEmpty() ? null : events.get(0).getOccurredAt())
            .eventCount((long) events.size())
            .build();
    }
    
    public BigDecimal getBalance(UUID accountId) {
        BigDecimal balance = projectionService.getBalance(accountId);
        if (balance == null) {
            throw new AccountNotFoundException("Account not found: " + accountId);
        }
        return balance;
    }
}
