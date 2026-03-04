package com.fintech.ledger.repository;

import com.fintech.ledger.domain.AccountBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AccountBalanceRepository extends JpaRepository<AccountBalance, UUID> {
}
