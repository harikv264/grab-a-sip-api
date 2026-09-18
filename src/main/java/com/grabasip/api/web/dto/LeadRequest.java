package com.grabasip.api.web.dto;

/** Payload posted when a serviceability check is recorded as a lead. */
public record LeadRequest(
        String rawLocation,
        String pincode,
        String matchedArea,
        Boolean serviceable,
        String phone
) {
}
