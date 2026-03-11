package com.swifttrack.BillingAndSettlementService.repositories;

import com.swifttrack.BillingAndSettlementService.models.Account;
import com.swifttrack.BillingAndSettlementService.models.enums.AccountType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccountRepository extends JpaRepository<Account, UUID> {

    Optional<Account> findByUserIdAndAccountType(UUID userId, AccountType accountType);

    Account findByUserId(UUID userId);

    List<Account> findByAccountType(AccountType accountType);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Account a WHERE a.userId = :userId AND a.accountType = :accountType")
    Optional<Account> findByUserIdAndAccountTypeForUpdate(
            @Param("userId") UUID userId,
            @Param("accountType") AccountType accountType);
}
