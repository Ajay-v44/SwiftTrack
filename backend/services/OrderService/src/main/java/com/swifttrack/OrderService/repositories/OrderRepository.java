package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDateTime;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Modifying;
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

  @EntityGraph(attributePaths = { "locations", "trackingState" })
  Optional<Order> findByCustomerReferenceId(String customerReferenceId);

  @EntityGraph(attributePaths = { "locations", "trackingState" })
  Optional<Order> findByProviderOrderId(String providerOrderId);

  @Query(value = """
      SELECT *
      FROM orders
      WHERE CAST(id AS text) ILIKE CONCAT(:idPrefix, '%')
      ORDER BY created_at DESC
      """, nativeQuery = true)
  List<Order> findByIdPrefix(@Param("idPrefix") String idPrefix);

  // Find by Order Status
  List<Order> findByOrderStatus(OrderStatus orderStatus);

  // Find by Tenant and Status
  Page<Order> findByTenantIdAndOrderStatus(UUID tenantId, OrderStatus orderStatus, Pageable pageable);

  long countByTenantIdAndOrderStatus(UUID tenantId, OrderStatus orderStatus);

  @Query("SELECT COUNT(o) FROM Order o WHERE o.tenantId = :tenantId AND o.orderStatus NOT IN :statuses")
  long countByTenantIdAndOrderStatusNotIn(@Param("tenantId") UUID tenantId,
      @Param("statuses") List<OrderStatus> statuses);

  List<Order> findTop3ByTenantIdOrderByCreatedAtDesc(UUID tenantId);

  List<Order> findByTenantIdAndCreatedAtGreaterThanEqualOrderByCreatedAtAsc(UUID tenantId, LocalDateTime createdAt);

  List<Order> findByTenantIdAndOrderStatusAndUpdatedAtGreaterThanEqualOrderByUpdatedAtAsc(
      UUID tenantId,
      OrderStatus orderStatus,
      LocalDateTime updatedAt);

  @Query("""
      SELECT FUNCTION('DATE', o.updatedAt), COUNT(o)
      FROM Order o
      WHERE o.tenantId = :tenantId
        AND o.orderStatus = :orderStatus
        AND o.updatedAt >= :startDateTime
        AND o.updatedAt < :endDateTime
      GROUP BY FUNCTION('DATE', o.updatedAt)
      ORDER BY FUNCTION('DATE', o.updatedAt)
      """)
  List<Object[]> countDeliveredOrdersByTenantIdGroupedByDay(
      @Param("tenantId") UUID tenantId,
      @Param("orderStatus") OrderStatus orderStatus,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime);

  Optional<Order> findById(UUID orderId);

  @EntityGraph(attributePaths = { "locations", "trackingState" })
  @Query("SELECT o FROM Order o WHERE o.id = :orderId")
  Optional<Order> findDetailedById(@Param("orderId") UUID orderId);

  // Search by Customer Reference ID (Partial Match)
  @Query("SELECT o FROM Order o WHERE o.tenantId = :tenantId AND o.customerReferenceId LIKE %:query%")
  Page<Order> searchByCustomerReferenceId(@Param("tenantId") UUID tenantId, @Param("query") String query,
      Pageable pageable);

  @EntityGraph(attributePaths = "locations")
  @Query(value = """
      SELECT o
      FROM Order o
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
      ORDER BY o.createdAt DESC
      """, countQuery = """
      SELECT COUNT(o.id)
      FROM Order o
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
      """)
  Page<Order> findTenantOrders(
      @Param("tenantId") UUID tenantId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      Pageable pageable);

  @EntityGraph(attributePaths = "locations")
  @Query(value = """
      SELECT o
      FROM Order o
      WHERE o.ownerUserId = :ownerUserId
        AND o.bookingChannel = com.swifttrack.enums.BillingAndSettlement.BookingChannel.CONSUMER
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
      ORDER BY o.createdAt DESC
      """, countQuery = """
      SELECT COUNT(o.id)
      FROM Order o
      WHERE o.ownerUserId = :ownerUserId
        AND o.bookingChannel = com.swifttrack.enums.BillingAndSettlement.BookingChannel.CONSUMER
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
      """)
  Page<Order> findConsumerOrders(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      Pageable pageable);

  @EntityGraph(attributePaths = "locations")
  @Query(value = """
      SELECT DISTINCT o
      FROM Order o
      LEFT JOIN o.locations l
      WHERE o.ownerUserId = :ownerUserId
        AND o.bookingChannel = com.swifttrack.enums.BillingAndSettlement.BookingChannel.CONSUMER
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND (
          (:orderId IS NOT NULL AND o.id = :orderId) OR
          LOWER(COALESCE(o.customerReferenceId, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(o.selectedProviderCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.city, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.pincode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.locality, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      ORDER BY o.createdAt DESC
      """, countQuery = """
      SELECT COUNT(DISTINCT o.id)
      FROM Order o
      LEFT JOIN o.locations l
      WHERE o.ownerUserId = :ownerUserId
        AND o.bookingChannel = com.swifttrack.enums.BillingAndSettlement.BookingChannel.CONSUMER
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND (
          (:orderId IS NOT NULL AND o.id = :orderId) OR
          LOWER(COALESCE(o.customerReferenceId, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(o.selectedProviderCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.city, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.pincode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.locality, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      """)
  Page<Order> searchConsumerOrders(
      @Param("ownerUserId") UUID ownerUserId,
      @Param("query") String query,
      @Param("orderId") UUID orderId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      Pageable pageable);

  @EntityGraph(attributePaths = "locations")
  @Query(value = """
      SELECT DISTINCT o
      FROM Order o
      LEFT JOIN o.locations l
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND (
          (:orderId IS NOT NULL AND o.id = :orderId) OR
          LOWER(COALESCE(o.customerReferenceId, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(o.selectedProviderCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.city, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.country, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.pincode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.locality, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      ORDER BY o.createdAt DESC
      """, countQuery = """
      SELECT COUNT(DISTINCT o.id)
      FROM Order o
      LEFT JOIN o.locations l
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND (
          (:orderId IS NOT NULL AND o.id = :orderId) OR
          LOWER(COALESCE(o.customerReferenceId, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(o.selectedProviderCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.city, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.country, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.pincode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.locality, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
      """)
  Page<Order> searchTenantOrders(
      @Param("tenantId") UUID tenantId,
      @Param("query") String query,
      @Param("orderId") UUID orderId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      Pageable pageable);

  @Query("""
      SELECT COUNT(o.id)
      FROM Order o
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND o.orderStatus IN :statuses
      """)
  long countTenantOrdersByStatuses(
      @Param("tenantId") UUID tenantId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      @Param("statuses") List<OrderStatus> statuses);

  @Query("""
      SELECT COUNT(DISTINCT o.id)
      FROM Order o
      LEFT JOIN o.locations l
      WHERE o.tenantId = :tenantId
        AND o.createdAt >= :startDateTime
        AND o.createdAt < :endDateTime
        AND (
          (:orderId IS NOT NULL AND o.id = :orderId) OR
          LOWER(COALESCE(o.customerReferenceId, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(o.selectedProviderCode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.city, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.state, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.country, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.pincode, '')) LIKE LOWER(CONCAT('%', :query, '%')) OR
          LOWER(COALESCE(l.locality, '')) LIKE LOWER(CONCAT('%', :query, '%'))
        )
        AND o.orderStatus IN :statuses
      """)
  long countSearchedTenantOrdersByStatuses(
      @Param("tenantId") UUID tenantId,
      @Param("query") String query,
      @Param("orderId") UUID orderId,
      @Param("startDateTime") LocalDateTime startDateTime,
      @Param("endDateTime") LocalDateTime endDateTime,
      @Param("statuses") List<OrderStatus> statuses);

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

  // Find by Order ID and Created By (User ID)
  @Query("SELECT o FROM Order o WHERE o.id=:orderId AND o.createdBy=:userId")
  Optional<Order> findById(@Param("orderId") UUID orderId, @Param("userId") UUID userId);

  // Update Order Status
  @Modifying(clearAutomatically = true)
  @org.springframework.transaction.annotation.Transactional
  @Query("UPDATE Order o SET o.orderStatus=:orderStatus, o.updatedAt = CURRENT_TIMESTAMP WHERE o.id=:orderId")
  int updateOrderStatus(@Param("orderId") UUID orderId, @Param("orderStatus") OrderStatus orderStatus);

  // Find orders by list of IDs
  @Query("SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.locations WHERE o.id IN :ids")
  List<Order> findByIdIn(@Param("ids") List<UUID> ids);
}
