package com.fintech.ledger.service;

import com.fintech.ledger.domain.EventType;
import com.fintech.ledger.domain.LedgerEvent;
import com.fintech.ledger.exception.ConcurrencyException;
import com.fintech.ledger.repository.LedgerEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventStoreService {
    
    private final LedgerEventRepository eventRepository;
    
    @Transactional
    public LedgerEvent appendEvent(UUID accountId, EventType eventType, BigDecimal amount, 
                                   String currency, Map<String, Object> metadata) {
        // Get current version
        Long currentVersion = eventRepository.findMaxVersionByAccountId(accountId).orElse(0L);
        Long nextVersion = currentVersion + 1;
        
        LedgerEvent event = LedgerEvent.builder()
            .accountId(accountId)
            .eventType(eventType)
            .amount(amount)
            .currency(currency)
            .metadata(metadata)
            .occurredAt(Instant.now())
            .version(nextVersion)
            .build();
        
        try {
            LedgerEvent saved = eventRepository.save(event);
            log.info("Appended event: {} for account: {}, version: {}", 
                eventType, accountId, nextVersion);
            return saved;
        } catch (DataIntegrityViolationException e) {
            log.error("Concurrency conflict for account: {}, version: {}", accountId, nextVersion);
            throw new ConcurrencyException("Concurrent modification detected. Please retry.");
        }
    }
    
    public List<LedgerEvent> getAccountEvents(UUID accountId) {
        return eventRepository.findByAccountIdOrderByVersionAsc(accountId);
    }
    
    public List<LedgerEvent> getAllEvents() {
        return eventRepository.findAllByOrderByOccurredAtAsc();
    }
    
    public Long getCurrentVersion(UUID accountId) {
        return eventRepository.findMaxVersionByAccountId(accountId).orElse(0L);
    }
}
