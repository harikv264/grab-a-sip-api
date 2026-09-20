package com.grabasip.api.web.dto;

import java.util.UUID;

/** Link a Supabase auth user (id) to a role + owned record. */
public record AppUserLinkRequest(
        UUID id,
        String role,
        UUID customerId,
        UUID deliveryPersonId,
        String email,
        String phone
) {
}
