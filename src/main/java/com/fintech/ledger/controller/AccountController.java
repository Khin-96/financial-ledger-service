package com.fintech.ledger.controller;

import com.fintech.ledger.domain.LedgerEvent;
import com.fintech.ledger.dto.*;
import com.fintech.ledger.service.AccountService;
import com.fintech.ledger.service.EventStoreService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Accounts", description = "Account management endpoints")
public class AccountController {
    
    private final AccountService accountService;
    private final EventStoreService eventStoreService;
    
    @PostMapping
    @Operation(summary = "Create a new account")
    public ResponseEntity<AccountResponse> createAccount(@Valid @RequestBody CreateAccountRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get account details")
    public ResponseEntity<AccountResponse> getAccount(@PathVariable UUID id) {
        AccountResponse response = accountService.getAccount(id);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}/balance")
    @Operation(summary = "Get current balance")
    public ResponseEntity<BigDecimal> getBalance(@PathVariable UUID id) {
        BigDecimal balance = accountService.getBalance(id);
        return ResponseEntity.ok(balance);
    }
    
    @GetMapping("/{id}/events")
    @Operation(summary = "Get event history (event store)")
    public ResponseEntity<List<EventResponse>> getEvents(@PathVariable UUID id) {
        List<LedgerEvent> events = eventStoreService.getAccountEvents(id);
        List<EventResponse> response = events.stream()
            .map(e -> EventResponse.builder()
                .eventId(e.getId())
                .accountId(e.getAccountId())
                .eventType(e.getEventType())
                .amount(e.getAmount())
                .currency(e.getCurrency())
                .metadata(e.getMetadata())
                .occurredAt(e.getOccurredAt())
                .version(e.getVersion())
                .build())
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
}
