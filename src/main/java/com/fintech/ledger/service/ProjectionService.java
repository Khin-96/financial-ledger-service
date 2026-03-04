package com.fintech.ledger.service;

import com.fintech.ledger.domain.AccountBalance;
import com.fintech.ledger.domain.EventType;
import com.fintech.ledger.domain.LedgerEvent;
import com.fintech.ledger.repository.AccountBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProjectionService {
    
    private final AccountBalanceRepository balanceRepository;
    private final EventStoreService eventStoreService;
    
    @Transactional
    public void projectEvent(LedgerEvent event) {
        AccountBalance balance = balanceRepository.findById(event.getAccountId())
            .orElseGet(() -> createNewBalance(event));
        
        // Apply event to balance
        switch (event.getEventType()) {
            case ACCOUNT_OPENED:
                // Balance already initialized
                break;
            case MONEY_DEPOSITED:
                balance.setBalance(balance.getBalance().add(event.getAmount()));
                break;
            case MONEY_WITHDRAWN:
                balance.setBalance(balance.getBalance().subtract(event.getAmount()));
                break;
            case TRANSFER_INITIATED:
            case TRANSFER_COMPLETED:
            case TRANSFER_FAILED:
            case TRANSFER_REVERSED:
                // These are handled by specific deposit/withdraw events
                break;
        }
        
        balance.setLastEventId(event.getId());
        balance.setLastEventVersion(event.getVersion());
        balance.setUpdatedAt(Instant.now());
        
        balanceRepository.save(balance);
        log.debug("Projected event {} for account {}, new balance: {}", 
            event.getEventType(), event.getAccountId(), balance.getBalance());
    }
    
    @Transactional
    public void rebuildAllProjections() {
        log.info("Starting projection rebuild...");
        
        // Clear all balances
        balanceRepository.deleteAll();
        
        // Replay all events
        List<LedgerEvent> events = eventStoreService.getAllEvents();
        for (LedgerEvent event : events) {
            projectEvent(event);
        }
        
        log.info("Projection rebuild completed. Processed {} events", events.size());
    }
    
    public BigDecimal getBalance(UUID accountId) {
        return balanceRepository.findById(accountId)
            .map(AccountBalance::getBalance)
            .orElse(BigDecimal.ZERO);
    }
    
    public AccountBalance getAccountBalance(UUID accountId) {
        return balanceRepository.findById(accountId).orElse(null);
    }
    
    private AccountBalance createNewBalance(LedgerEvent event) {
        return AccountBalance.builder()
            .accountId(event.getAccountId())
            .balance(BigDecimal.ZERO)
            .currency(event.getCurrency())
            .lastEventId(event.getId())
            .lastEventVersion(event.getVersion())
            .updatedAt(Instant.now())
            .build();
    }
}
