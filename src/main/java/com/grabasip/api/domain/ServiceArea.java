package com.grabasip.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * A locality we deliver to. Seeded on startup for future admin management.
 * (The live check currently uses the in-code list in ServiceabilityService,
 * kept in sync with the frontend.)
 */
@Entity
@Table(name = "service_areas")
public class ServiceArea {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String city;

    private String pincode;

    private String locality;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public ServiceArea() {
    }

    public ServiceArea(String city, String locality) {
        this.city = city;
        this.locality = locality;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
