package com.fintech.ledger.domain;

public enum EventType {
    ACCOUNT_OPENED,
    MONEY_DEPOSITED,
    MONEY_WITHDRAWN,
    TRANSFER_INITIATED,
    TRANSFER_COMPLETED,
    TRANSFER_FAILED,
    TRANSFER_REVERSED
}
