package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.UserAddress;
import com.swifttrack.OrderService.models.enums.AddressOwnerType;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, UUID> {

    List<UserAddress> findByTenantIdAndOwnerTypeOrderByIsDefaultDescCreatedAtDesc(UUID tenantId, AddressOwnerType ownerType);

    List<UserAddress> findByOwnerUserIdAndOwnerTypeOrderByIsDefaultDescCreatedAtDesc(UUID ownerUserId,
            AddressOwnerType ownerType);

    Optional<UserAddress> findByIdAndTenantIdAndOwnerType(UUID id, UUID tenantId, AddressOwnerType ownerType);

    Optional<UserAddress> findByIdAndOwnerUserIdAndOwnerType(UUID id, UUID ownerUserId, AddressOwnerType ownerType);
}
