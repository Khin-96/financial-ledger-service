package com.fintech.ledger.dto;

import com.fintech.ledger.domain.EventType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventResponse {
    private UUID eventId;
    private UUID accountId;
    private EventType eventType;
    private BigDecimal amount;
    private String currency;
    private Map<String, Object> metadata;
    private Instant occurredAt;
    private Long version;
}
