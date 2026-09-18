package com.grabasip.api.service;

import com.grabasip.api.domain.ServiceArea;
import com.grabasip.api.repo.ServiceAreaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Seeds the service_areas table with the served localities on first run,
 * so the DB reflects where we deliver. Idempotent: skips if already seeded.
 */
@Component
public class ServiceAreaSeeder implements CommandLineRunner {

    private final ServiceAreaRepository repo;
    private final ServiceabilityService serviceability;

    public ServiceAreaSeeder(ServiceAreaRepository repo, ServiceabilityService serviceability) {
        this.repo = repo;
        this.serviceability = serviceability;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        for (String locality : serviceability.localityOptions()) {
            repo.save(new ServiceArea(ServiceabilityService.CITY, locality));
        }
    }
}
