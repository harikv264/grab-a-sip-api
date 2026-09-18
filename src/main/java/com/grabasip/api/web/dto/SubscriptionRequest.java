package com.grabasip.api.web.dto;

import java.time.LocalDate;
import java.util.UUID;

/** Create/update payload for a subscription. */
public record SubscriptionRequest(
        UUID customerId,
        String planCode,
        LocalDate startDate,
        String status,
        String notes
) {
}
