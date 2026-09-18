package com.grabasip.api.repo;

import com.grabasip.api.domain.SubscriptionPause;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SubscriptionPauseRepository extends JpaRepository<SubscriptionPause, UUID> {
    List<SubscriptionPause> findBySubscriptionIdOrderByCreatedAtDesc(UUID subscriptionId);
}
