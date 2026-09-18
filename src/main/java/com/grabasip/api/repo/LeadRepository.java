package com.grabasip.api.repo;

import com.grabasip.api.domain.Lead;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findAllByOrderByCreatedAtDesc();
    List<Lead> findByServiceableOrderByCreatedAtDesc(boolean serviceable);
}
