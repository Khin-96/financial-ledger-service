package com.fintech.ledger.service;

import com.fintech.ledger.domain.EventType;
import com.fintech.ledger.domain.LedgerEvent;
import com.fintech.ledger.domain.Transfer;
import com.fintech.ledger.domain.TransferStatus;
import com.fintech.ledger.dto.TransferRequest;
import com.fintech.ledger.dto.TransferResponse;
import com.fintech.ledger.exception.InsufficientFundsException;
import com.fintech.ledger.repository.TransferRepository;
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
public class TransferService {
    
    private final EventStoreService eventStoreService;
    private final ProjectionService projectionService;
    private final TransferRepository transferRepository;
    
    /**
     * Transfer Saga Pattern:
     * 1. Create transfer record (INITIATED)
     * 2. Emit TRANSFER_INITIATED event
     * 3. Withdraw from source account
     * 4. Deposit to destination account
     * 5. Mark transfer as COMPLETED
     * 
     * If any step fails:
     * - Reverse the withdrawal (compensating transaction)
     * - Mark transfer as FAILED
     */
    @Transactional
    public TransferResponse transfer(TransferRequest request) {
        // Step 1: Create transfer record
        Transfer transfer = Transfer.builder()
            .fromAccountId(request.getFromAccountId())
            .toAccountId(request.getToAccountId())
            .amount(request.getAmount())
            .currency("USD")
            .status(TransferStatus.INITIATED)
            .build();
        
        transfer = transferRepository.save(transfer);
        log.info("Transfer initiated: {}", transfer.getId());
        
        try {
            // Step 2: Emit TRANSFER_INITIATED event
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("transferId", transfer.getId().toString());
            metadata.put("description", request.getDescription());
            
            eventStoreService.appendEvent(
                request.getFromAccountId(),
                EventType.TRANSFER_INITIATED,
                request.getAmount(),
                "USD",
                metadata
            );
            
            // Step 3: Withdraw from source account
            BigDecimal sourceBalance = projectionService.getBalance(request.getFromAccountId());
            if (sourceBalance.compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException("Insufficient funds for transfer");
            }
            
            LedgerEvent withdrawEvent = eventStoreService.appendEvent(
                request.getFromAccountId(),
                EventType.MONEY_WITHDRAWN,
                request.getAmount(),
                "USD",
                metadata
            );
            projectionService.projectEvent(withdrawEvent);
            
            // Step 4: Deposit to destination account
            LedgerEvent depositEvent = eventStoreService.appendEvent(
                request.getToAccountId(),
                EventType.MONEY_DEPOSITED,
                request.getAmount(),
                "USD",
                metadata
            );
            projectionService.projectEvent(depositEvent);
            
            // Step 5: Mark transfer as COMPLETED
            transfer.setStatus(TransferStatus.COMPLETED);
            transfer.setCompletedAt(Instant.now());
            transferRepository.save(transfer);
            
            eventStoreService.appendEvent(
                request.getFromAccountId(),
                EventType.TRANSFER_COMPLETED,
                request.getAmount(),
                "USD",
                metadata
            );
            
            log.info("Transfer completed: {}", transfer.getId());
            
        } catch (Exception e) {
            log.error("Transfer failed: {}", transfer.getId(), e);
            
            // Compensating transaction: Reverse the withdrawal
            try {
                Map<String, Object> reversalMetadata = new HashMap<>();
                reversalMetadata.put("transferId", transfer.getId().toString());
                reversalMetadata.put("reason", "Transfer failed: " + e.getMessage());
                
                LedgerEvent reversalEvent = eventStoreService.appendEvent(
                    request.getFromAccountId(),
                    EventType.TRANSFER_REVERSED,
                    request.getAmount(),
                    "USD",
                    reversalMetadata
                );
                projectionService.projectEvent(reversalEvent);
                
                log.info("Transfer reversed: {}", transfer.getId());
            } catch (Exception reversalError) {
                log.error("Failed to reverse transfer: {}", transfer.getId(), reversalError);
            }
            
            // Mark transfer as FAILED
            transfer.setStatus(TransferStatus.FAILED);
            transfer.setFailureReason(e.getMessage());
            transfer.setCompletedAt(Instant.now());
            transferRepository.save(transfer);
            
            throw e;
        }
        
        return TransferResponse.builder()
            .transferId(transfer.getId())
            .fromAccountId(transfer.getFromAccountId())
            .toAccountId(transfer.getToAccountId())
            .amount(transfer.getAmount())
            .currency(transfer.getCurrency())
            .status(transfer.getStatus())
            .failureReason(transfer.getFailureReason())
            .createdAt(transfer.getCreatedAt())
            .completedAt(transfer.getCompletedAt())
            .build();
    }
    
    public TransferResponse getTransfer(UUID transferId) {
        Transfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new RuntimeException("Transfer not found"));
        
        return TransferResponse.builder()
            .transferId(transfer.getId())
            .fromAccountId(transfer.getFromAccountId())
            .toAccountId(transfer.getToAccountId())
            .amount(transfer.getAmount())
            .currency(transfer.getCurrency())
            .status(transfer.getStatus())
            .failureReason(transfer.getFailureReason())
            .createdAt(transfer.getCreatedAt())
            .completedAt(transfer.getCompletedAt())
            .build();
    }
}
