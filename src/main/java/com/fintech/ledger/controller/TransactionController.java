package com.fintech.ledger.controller;

import com.fintech.ledger.dto.DepositRequest;
import com.fintech.ledger.dto.TransferRequest;
import com.fintech.ledger.dto.TransferResponse;
import com.fintech.ledger.dto.WithdrawRequest;
import com.fintech.ledger.service.AccountService;
import com.fintech.ledger.service.TransferService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Transaction endpoints")
public class TransactionController {
    
    private final AccountService accountService;
    private final TransferService transferService;
    
    @PostMapping("/deposit")
    @Operation(summary = "Deposit funds to account")
    public ResponseEntity<Void> deposit(@Valid @RequestBody DepositRequest request) {
        accountService.deposit(request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/withdraw")
    @Operation(summary = "Withdraw funds from account")
    public ResponseEntity<Void> withdraw(@Valid @RequestBody WithdrawRequest request) {
        accountService.withdraw(request);
        return ResponseEntity.ok().build();
    }
    
    @PostMapping("/transfer")
    @Operation(summary = "Transfer funds between accounts")
    public ResponseEntity<TransferResponse> transfer(@Valid @RequestBody TransferRequest request) {
        TransferResponse response = transferService.transfer(request);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get transfer status")
    public ResponseEntity<TransferResponse> getTransfer(@PathVariable UUID id) {
        TransferResponse response = transferService.getTransfer(id);
        return ResponseEntity.ok(response);
    }
}
