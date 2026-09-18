package com.grabasip.api.repo;

import com.grabasip.api.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findAllByOrderByCreatedAtDesc();
    List<Subscription> findByStatusOrderByCreatedAtDesc(String status);
    List<Subscription> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
