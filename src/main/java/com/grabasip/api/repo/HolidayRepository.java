package com.grabasip.api.repo;

import com.grabasip.api.domain.Holiday;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface HolidayRepository extends JpaRepository<Holiday, UUID> {
    boolean existsByDate(LocalDate date);
    List<Holiday> findAllByOrderByDateDesc();
    long countByDateBetween(LocalDate start, LocalDate end);
}
