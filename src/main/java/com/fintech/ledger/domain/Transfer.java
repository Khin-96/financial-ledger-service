package com.fintech.ledger.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "transfers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "from_account_id", nullable = false)
    private UUID fromAccountId;
    
    @Column(name = "to_account_id", nullable = false)
    private UUID toAccountId;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;
    
    @Column(name = "failure_reason")
    private String failureReason;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "completed_at")
    private Instant completedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
        if (status == null) {
            status = TransferStatus.INITIATED;
        }
    }
}
