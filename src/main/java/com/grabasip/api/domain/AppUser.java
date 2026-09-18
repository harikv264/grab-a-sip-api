package com.grabasip.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

/**
 * Maps a Supabase auth user (id = the JWT `sub`) to a role and, for riders /
 * customers, the record they own. This is the entitlements source of truth.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    /** = Supabase auth user id (the JWT subject). Not auto-generated. */
    @Id
    private UUID id;

    /** admin | rider | customer  (future: vendor | sourcing_agent) */
    @Column(nullable = false)
    private String role;

    @Column(name = "customer_id")
    private UUID customerId;

    @Column(name = "delivery_person_id")
    private UUID deliveryPersonId;

    private String email;
    private String phone;

    @Column(nullable = false)
    private boolean active = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    public AppUser() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    public UUID getDeliveryPersonId() { return deliveryPersonId; }
    public void setDeliveryPersonId(UUID deliveryPersonId) { this.deliveryPersonId = deliveryPersonId; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
