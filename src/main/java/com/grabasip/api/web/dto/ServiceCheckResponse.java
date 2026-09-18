package com.grabasip.api.web.dto;

/** Result of a serviceability check. status = serviceable | not_serviceable | ask_again */
public record ServiceCheckResponse(
        String status,
        String matchedArea,
        String pincode,
        String message
) {
    public static ServiceCheckResponse serviceable(String area, String pincode) {
        return new ServiceCheckResponse("serviceable", area, pincode, null);
    }
    public static ServiceCheckResponse notServiceable() {
        return new ServiceCheckResponse("not_serviceable", null, null, null);
    }
    public static ServiceCheckResponse askAgain(String message) {
        return new ServiceCheckResponse("ask_again", null, null, message);
    }
}
