package com.swifttrack.OrderService.repositories;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.swifttrack.OrderService.models.OrderLocation;
import com.swifttrack.OrderService.models.enums.LocationType;

@Repository
public interface OrderLocationRepository extends JpaRepository<OrderLocation, UUID> {

    List<OrderLocation> findByOrderId(UUID orderId);

    List<OrderLocation> findByOrderIdAndLocationType(UUID orderId, LocationType locationType);
}
