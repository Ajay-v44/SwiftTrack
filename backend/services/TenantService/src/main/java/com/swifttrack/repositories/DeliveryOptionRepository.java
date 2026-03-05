package com.swifttrack.repositories;

import com.swifttrack.Models.DeliveryOption;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryOptionRepository extends JpaRepository<DeliveryOption, UUID> {

    List<DeliveryOption> findByActiveTrue();

    List<DeliveryOption> findByOptionTypeIn(Collection<String> optionTypes);

    Optional<DeliveryOption> findByOptionTypeIgnoreCase(String optionType);
}
