package com.fintech.ledger.repository;

import com.fintech.ledger.domain.LedgerEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerEventRepository extends JpaRepository<LedgerEvent, UUID> {
    List<LedgerEvent> findByAccountIdOrderByVersionAsc(UUID accountId);
    List<LedgerEvent> findAllByOrderByOccurredAtAsc();
    
    @Query("SELECT MAX(e.version) FROM LedgerEvent e WHERE e.accountId = :accountId")
    Optional<Long> findMaxVersionByAccountId(UUID accountId);
}
