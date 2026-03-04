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
@Table(name = "account_balances")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountBalance {
    
    @Id
    @Column(name = "account_id")
    private UUID accountId;
    
    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;
    
    @Column(nullable = false, length = 3)
    private String currency;
    
    @Column(name = "last_event_id")
    private UUID lastEventId;
    
    @Column(name = "last_event_version")
    private Long lastEventVersion;
    
    @Column(name = "updated_at")
    private Instant updatedAt;
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
