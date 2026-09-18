package com.grabasip.api.web;

import com.grabasip.api.service.ServiceabilityService;
import com.grabasip.api.web.dto.ServiceCheckResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/serviceability")
public class ServiceabilityController {

    private final ServiceabilityService service;

    public ServiceabilityController(ServiceabilityService service) {
        this.service = service;
    }

    /** GET /api/serviceability/check?q=Gachibowli */
    @GetMapping("/check")
    public ServiceCheckResponse check(@RequestParam(name = "q", defaultValue = "") String q) {
        return service.check(q);
    }

    /** GET /api/serviceability/areas — the served-locality list (for autocomplete). */
    @GetMapping("/areas")
    public List<String> areas() {
        return service.localityOptions();
    }
}
