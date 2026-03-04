package com.fintech.ledger.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "ledger_events", indexes = {
    @Index(name = "idx_account_id", columnList = "account_id"),
    @Index(name = "idx_occurred_at", columnList = "occurred_at")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_account_version", columnNames = {"account_id", "version"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerEvent {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(name = "account_id", nullable = false)
    private UUID accountId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private EventType eventType;
    
    @Column(precision = 19, scale = 4)
    private BigDecimal amount;
    
    @Column(length = 3)
    private String currency;
    
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> metadata;
    
    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;
    
    @Column(nullable = false)
    private Long version;
    
    @PrePersist
    protected void onCreate() {
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
    }
}
