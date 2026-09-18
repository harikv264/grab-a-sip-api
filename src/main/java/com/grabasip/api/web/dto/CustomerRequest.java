package com.grabasip.api.web.dto;

/** Create/update payload for a customer. */
public record CustomerRequest(
        String name,
        String phone,
        String flatHouse,
        String street,
        String locality,
        String pincode,
        String landmark,
        String addressStatus,
        String source,
        String notes,
        String convertedFromLeadId
) {
}
