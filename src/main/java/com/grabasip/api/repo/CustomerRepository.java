package com.grabasip.api.repo;

import com.grabasip.api.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    Optional<Customer> findByPhone(String phone);

    List<Customer> findAllByOrderByCreatedAtDesc();

    @Query("""
            select c from Customer c
            where lower(c.name) like lower(concat('%', :q, '%'))
               or c.phone like concat('%', :q, '%')
               or lower(c.locality) like lower(concat('%', :q, '%'))
            order by c.createdAt desc
            """)
    List<Customer> search(@Param("q") String q);
}
