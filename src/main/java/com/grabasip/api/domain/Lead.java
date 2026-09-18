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
 * A serviceability check recorded as a lead. Rows where isServiceable=false
 * are the "undelivered areas" / expansion demand. Columns match the Supabase
 * schema and the Next.js /api/leads writer so both can share the table.
 */
@Entity
@Table(name = "leads")
public class Lead {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "raw_location")
    private String rawLocation;

    @Column(name = "pincode")
    private String pincode;

    @Column(name = "matched_area")
    private String matchedArea;

    @Column(name = "is_serviceable", nullable = false)
    private boolean serviceable;

    @Column(name = "phone")
    private String phone;

    @Column(name = "converted_customer_id")
    private UUID convertedCustomerId;

    public Lead() {
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

    public String getRawLocation() { return rawLocation; }
    public void setRawLocation(String rawLocation) { this.rawLocation = rawLocation; }

    public String getPincode() { return pincode; }
    public void setPincode(String pincode) { this.pincode = pincode; }

    public String getMatchedArea() { return matchedArea; }
    public void setMatchedArea(String matchedArea) { this.matchedArea = matchedArea; }

    public boolean isServiceable() { return serviceable; }
    public void setServiceable(boolean serviceable) { this.serviceable = serviceable; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public UUID getConvertedCustomerId() { return convertedCustomerId; }
    public void setConvertedCustomerId(UUID convertedCustomerId) { this.convertedCustomerId = convertedCustomerId; }
}
