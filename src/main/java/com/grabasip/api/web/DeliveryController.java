package com.grabasip.api.web;

import com.grabasip.api.domain.Customer;
import com.grabasip.api.domain.Delivery;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.DeliveryRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.service.DeliveryGenerationService;
import com.grabasip.api.service.DeliveryGenerationService.GenerateResult;
import com.grabasip.api.web.dto.DeliveryUpdateRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private static final Set<String> STATUSES = Set.of("pending", "dispatched", "delivered", "failed");

    private final DeliveryRepository deliveries;
    private final CustomerRepository customers;
    private final DeliveryGenerationService generation;
    private final AdminGuard admin;

    public DeliveryController(DeliveryRepository deliveries, CustomerRepository customers,
                              DeliveryGenerationService generation, AdminGuard admin) {
        this.deliveries = deliveries;
        this.customers = customers;
        this.generation = generation;
        this.admin = admin;
    }

    /** GET /api/deliveries?date=YYYY-MM-DD — the board for a day. */
    @GetMapping
    public List<Delivery> list(@RequestParam String token,
                               @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        admin.require(token);
        return deliveries.findByDateOrderByCustomerNameAsc(date);
    }

    /** GET /api/deliveries/summary?date=YYYY-MM-DD — counts for the day + month. */
    @GetMapping("/summary")
    public Map<String, Object> summary(@RequestParam String token,
                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        admin.require(token);
        List<Delivery> day = deliveries.findByDateOrderByCustomerNameAsc(date);
        long delivered = day.stream().filter(d -> "delivered".equals(d.getStatus())).count();
        long failed = day.stream().filter(d -> "failed".equals(d.getStatus())).count();
        long dispatched = day.stream().filter(d -> "dispatched".equals(d.getStatus())).count();
        long pending = day.stream().filter(d -> "pending".equals(d.getStatus())).count();

        LocalDate first = date.withDayOfMonth(1);
        LocalDate last = date.withDayOfMonth(date.lengthOfMonth());
        long monthDelivered = deliveries.countByDateBetweenAndStatus(first, last, "delivered");

        return Map.of(
                "total", day.size(),
                "pending", pending,
                "dispatched", dispatched,
                "delivered", delivered,
                "failed", failed,
                "monthDelivered", monthDelivered,
                "isDeliveryDay", generation.isDeliveryDay(date)
        );
    }

    /** POST /api/deliveries/generate?date=YYYY-MM-DD — create the day's deliveries. */
    @PostMapping("/generate")
    public GenerateResult generate(@RequestParam String token,
                                   @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        admin.require(token);
        return generation.generateForDate(date);
    }

    /** PUT /api/deliveries/{id} — update status / assignee / notes. */
    @PutMapping("/{id}")
    public Delivery update(@RequestParam String token, @PathVariable UUID id,
                           @RequestBody DeliveryUpdateRequest body) {
        admin.require(token);
        Delivery d = deliveries.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (body.status() != null && !body.status().isBlank()) {
            String s = body.status().trim().toLowerCase();
            if (!STATUSES.contains(s))
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
            d.setStatus(s);
            // First successful delivery verifies the customer's address.
            if ("delivered".equals(s)) {
                customers.findById(d.getCustomerId()).ifPresent((Customer c) -> {
                    if (!"verified".equals(c.getAddressStatus())) {
                        c.setAddressStatus("verified");
                        customers.save(c);
                    }
                });
            }
        }
        // deliveryPersonId: pass explicitly to set/clear
        d.setDeliveryPersonId(body.deliveryPersonId());
        if (body.notes() != null) {
            String n = body.notes().trim();
            d.setNotes(n.isEmpty() ? null : n);
        }
        return deliveries.save(d);
    }
}
