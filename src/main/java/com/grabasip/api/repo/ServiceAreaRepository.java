package com.grabasip.api.repo;

import com.grabasip.api.domain.ServiceArea;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServiceAreaRepository extends JpaRepository<ServiceArea, UUID> {
}
