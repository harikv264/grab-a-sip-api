package com.grabasip.api.repo;

import com.grabasip.api.domain.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    List<Delivery> findByDateOrderByCustomerNameAsc(LocalDate date);
    boolean existsBySubscriptionIdAndDate(UUID subscriptionId, LocalDate date);
    long countByDateBetweenAndStatus(LocalDate start, LocalDate end, String status);

    // Rider self-service
    List<Delivery> findByDeliveryPersonIdAndDateOrderByCustomerNameAsc(UUID deliveryPersonId, LocalDate date);
    long countByDeliveryPersonIdAndStatusAndDateBetween(UUID deliveryPersonId, String status, LocalDate start, LocalDate end);

    // Customer self-service
    List<Delivery> findByCustomerIdOrderByDateDesc(UUID customerId);
    long countByCustomerIdAndStatusAndDateBetween(UUID customerId, String status, LocalDate start, LocalDate end);
}
