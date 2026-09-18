package com.grabasip.api.web.dto;

public record DeliveryPersonRequest(
        String name,
        String phone,
        String area,
        Boolean active
) {
}
