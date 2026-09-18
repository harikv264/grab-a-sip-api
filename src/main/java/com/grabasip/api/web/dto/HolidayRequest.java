package com.grabasip.api.web.dto;

import java.time.LocalDate;

public record HolidayRequest(LocalDate date, String name) {
}
