package com.grabasip.api.web.dto;

import java.util.UUID;

public record DeliveryUpdateRequest(
        String status,
        UUID deliveryPersonId,
        String notes
) {
}
