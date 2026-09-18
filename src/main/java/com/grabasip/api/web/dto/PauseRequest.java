package com.grabasip.api.web.dto;

import java.time.LocalDate;

/** Record a pause window (days) against a subscription. */
public record PauseRequest(
        Integer days,
        LocalDate startDate,
        String note
) {
}
