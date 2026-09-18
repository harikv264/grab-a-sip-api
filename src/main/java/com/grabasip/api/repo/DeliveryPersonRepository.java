package com.grabasip.api.repo;

import com.grabasip.api.domain.DeliveryPerson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface DeliveryPersonRepository extends JpaRepository<DeliveryPerson, UUID> {
    List<DeliveryPerson> findAllByOrderByNameAsc();
}
