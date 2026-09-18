package com.grabasip.api.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;
import java.util.UUID;

/** A registered customer with a structured, validatable delivery address. */
@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String phone;

    // Structured address
    @Column(name = "flat_house")
    private String flatHouse;
    private String street;
    @Column(nullable = false)
    private String locality;
    private String pincode;
    private String landmark;

    /** pending | verified — verified after the first successful delivery (Phase 4). */
    @Column(name = "address_status", nullable = false)
    private String addressStatus = "pending";

    /** lead | whatsapp | manual */
    @Column(nullable = false)
    private String source = "manual";

    @Column(columnDefinition = "text")
    private String notes;

    @Column(name = "converted_from_lead_id")
    private UUID convertedFromLeadId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Instant updatedAt;

    public Customer() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getFlatHouse() { return flatHouse; }
    public void setFlatHouse(String flatHouse) { this.flatHouse = flatHouse; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getLocality() { return locality; }
    public void setLocality(String locality) { this.locality = locality; }
    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }
    public String getLandmark() { return landmark; }
    public void setLandmark(String landmark) { this.landmark = landmark; }
    public String getAddressStatus() { return addressStatus; }
    public void setAddressStatus(String addressStatus) { this.addressStatus = addressStatus; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public UUID getConvertedFromLeadId() { return convertedFromLeadId; }
    public void setConvertedFromLeadId(UUID convertedFromLeadId) { this.convertedFromLeadId = convertedFromLeadId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
