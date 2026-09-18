package com.grabasip.api.web;

import com.grabasip.api.domain.Customer;
import com.grabasip.api.domain.Lead;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.LeadRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.service.ServiceabilityService;
import com.grabasip.api.web.dto.CustomerRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerRepository customers;
    private final LeadRepository leads;
    private final ServiceabilityService serviceability;
    private final AdminGuard admin;

    public CustomerController(CustomerRepository customers, LeadRepository leads,
                              ServiceabilityService serviceability, AdminGuard admin) {
        this.customers = customers;
        this.leads = leads;
        this.serviceability = serviceability;
        this.admin = admin;
    }

    @GetMapping
    public List<Customer> list(@RequestParam String token,
                               @RequestParam(required = false) String q) {
        admin.require(token);
        if (q != null && !q.isBlank()) return customers.search(q.trim());
        return customers.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public Customer get(@RequestParam String token, @PathVariable UUID id) {
        admin.require(token);
        return customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @PostMapping
    public ResponseEntity<Customer> create(@RequestParam String token,
                                           @RequestBody CustomerRequest body) {
        admin.require(token);
        String name = required(body.name(), "name");
        String phone = normalizePhone(required(body.phone(), "phone"));
        String locality = validLocality(required(body.locality(), "locality"));

        customers.findByPhone(phone).ifPresent(c -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A customer with this phone already exists");
        });

        Customer c = new Customer();
        c.setName(name);
        c.setPhone(phone);
        c.setLocality(locality);
        applyOptional(c, body);
        c.setSource(blankTo(body.source(), "manual"));
        c.setAddressStatus(blankTo(body.addressStatus(), "pending"));

        UUID leadId = parseUuid(body.convertedFromLeadId());
        if (leadId != null) c.setConvertedFromLeadId(leadId);

        Customer saved = customers.save(c);

        // Link the originating lead, if any.
        if (leadId != null) {
            leads.findById(leadId).ifPresent((Lead l) -> {
                l.setConvertedCustomerId(saved.getId());
                leads.save(l);
            });
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public Customer update(@RequestParam String token, @PathVariable UUID id,
                           @RequestBody CustomerRequest body) {
        admin.require(token);
        Customer c = customers.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));

        c.setName(required(body.name(), "name"));
        String phone = normalizePhone(required(body.phone(), "phone"));
        customers.findByPhone(phone).ifPresent(other -> {
            if (!other.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "Another customer already uses this phone");
            }
        });
        c.setPhone(phone);
        c.setLocality(validLocality(required(body.locality(), "locality")));
        applyOptional(c, body);
        if (body.source() != null && !body.source().isBlank()) c.setSource(body.source());
        if (body.addressStatus() != null && !body.addressStatus().isBlank())
            c.setAddressStatus(body.addressStatus());

        return customers.save(c);
    }

    // ── helpers ──────────────────────────────────────────────────
    private void applyOptional(Customer c, CustomerRequest b) {
        c.setFlatHouse(trimOrNull(b.flatHouse()));
        c.setStreet(trimOrNull(b.street()));
        c.setLandmark(trimOrNull(b.landmark()));
        c.setNotes(trimOrNull(b.notes()));
        String pin = trimOrNull(b.pincode());
        if (pin != null && !pin.matches("\\d{6}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Pincode must be 6 digits");
        }
        c.setPincode(pin);
    }

    private String validLocality(String locality) {
        boolean ok = serviceability.localityOptions().stream()
                .anyMatch(l -> l.equalsIgnoreCase(locality.trim()));
        if (!ok) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Locality must be one we deliver to");
        }
        return serviceability.localityOptions().stream()
                .filter(l -> l.equalsIgnoreCase(locality.trim()))
                .findFirst().orElse(locality.trim());
    }

    private static String required(String v, String field) {
        if (v == null || v.trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, field + " is required");
        }
        return v.trim();
    }

    private static String normalizePhone(String v) {
        String digits = v.replaceAll("\\D", "");
        if (digits.length() < 8) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Enter a valid phone number");
        }
        return v.trim();
    }

    private static String trimOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static String blankTo(String v, String fallback) {
        return v == null || v.isBlank() ? fallback : v.trim();
    }

    private static UUID parseUuid(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return UUID.fromString(v.trim());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
