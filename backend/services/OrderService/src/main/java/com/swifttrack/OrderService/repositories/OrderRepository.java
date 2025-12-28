package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.Order;
import com.swifttrack.OrderService.models.enums.OrderStatus;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

        // Find by Tenant
        Page<Order> findByTenantId(UUID tenantId, Pageable pageable);

        // Find by Customer Reference ID (Tenant's Order ID)
        Optional<Order> findByTenantIdAndCustomerReferenceId(UUID tenantId, String customerReferenceId);

        // Find by Order Status
        List<Order> findByOrderStatus(OrderStatus orderStatus);

        // Find by Tenant and Status
        Page<Order> findByTenantIdAndOrderStatus(UUID tenantId, OrderStatus orderStatus, Pageable pageable);

        // Find by Provider Order ID
        Optional<Order> findByProviderOrderId(String providerOrderId);

        // Search by Customer Reference ID (Partial Match)
        @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.customerReferenceId LIKE %:query%")
        Page<Order> searchByCustomerReferenceId(@Param("tenantId") UUID tenantId, @Param("query") String query,
                        Pageable pageable);

        // Find active orders for a specific provider
        @Query("SELECT o FROM Order o WHERE o.selectedProviderCode = :providerCode AND o.orderStatus NOT IN ('DELIVERED', 'CANCELLED', 'FAILED')")
        List<Order> findActiveOrdersByProvider(@Param("providerCode") String providerCode);

        // Count active orders for a specific provider
        @Query("SELECT COUNT(o) FROM Order o WHERE o.selectedProviderCode = :providerCode AND o.orderStatus NOT IN ('DELIVERED', 'CANCELLED', 'FAILED')")
        int countActiveOrdersByProvider(@Param("providerCode") String providerCode);

        // Find orders by tenant and provider code
        @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.selectedProviderCode = :providerCode")
        List<Order> findByTenantIdAndProviderCode(@Param("tenantId") UUID tenantId,
                        @Param("providerCode") String providerCode);
}
