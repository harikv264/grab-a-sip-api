package com.grabasip.api.web;

import com.grabasip.api.domain.AppUser;
import com.grabasip.api.domain.Customer;
import com.grabasip.api.domain.Delivery;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.DeliveryRepository;
import com.grabasip.api.security.CurrentUserService;
import com.grabasip.api.web.dto.DeliveryUpdateRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** A delivery rider's own view — only their deliveries and their own stats. */
@RestController
@RequestMapping("/api/rider")
public class RiderController {

    private static final Set<String> STATUSES = Set.of("pending", "dispatched", "delivered", "failed");

    private final DeliveryRepository deliveries;
    private final CustomerRepository customers;
    private final CurrentUserService currentUser;

    public RiderController(DeliveryRepository deliveries, CustomerRepository customers,
                          CurrentUserService currentUser) {
        this.deliveries = deliveries;
        this.customers = customers;
        this.currentUser = currentUser;
    }

    /** The signed-in rider's linked delivery_person id, or 403. */
    private UUID riderId() {
        AppUser u = currentUser.current()
                .filter(a -> "rider".equalsIgnoreCase(a.getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Riders only"));
        if (u.getDeliveryPersonId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Rider account isn't linked yet");
        }
        return u.getDeliveryPersonId();
    }

    /** GET /api/rider/deliveries?date=YYYY-MM-DD — my deliveries for a day (defaults today). */
    @GetMapping("/deliveries")
    public List<Delivery> myDeliveries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        UUID rider = riderId();
        LocalDate d = date != null ? date : LocalDate.now();
        return deliveries.findByDeliveryPersonIdAndDateOrderByCustomerNameAsc(rider, d);
    }

    /** PUT /api/rider/deliveries/{id} — update status of one of MY deliveries. */
    @PutMapping("/deliveries/{id}")
    public Delivery updateMyDelivery(@PathVariable UUID id, @RequestBody DeliveryUpdateRequest body) {
        UUID rider = riderId();
        Delivery d = deliveries.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));
        if (!rider.equals(d.getDeliveryPersonId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your delivery");
        }
        if (body.status() != null && !body.status().isBlank()) {
            String s = body.status().trim().toLowerCase();
            if (!STATUSES.contains(s)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid status");
            }
            d.setStatus(s);
            if ("delivered".equals(s)) {
                customers.findById(d.getCustomerId()).ifPresent((Customer c) -> {
                    if (!"verified".equals(c.getAddressStatus())) {
                        c.setAddressStatus("verified");
                        customers.save(c);
                    }
                });
            }
        }
        if (body.notes() != null) {
            String n = body.notes().trim();
            d.setNotes(n.isEmpty() ? null : n);
        }
        return deliveries.save(d);
    }

    /** GET /api/rider/stats — my delivered counts for today / this week / this month. */
    @GetMapping("/stats")
    public Map<String, Object> myStats() {
        UUID rider = riderId();
        LocalDate today = LocalDate.now();
        LocalDate weekStart = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate monthStart = today.withDayOfMonth(1);

        long delToday = deliveries.countByDeliveryPersonIdAndStatusAndDateBetween(rider, "delivered", today, today);
        long delWeek = deliveries.countByDeliveryPersonIdAndStatusAndDateBetween(rider, "delivered", weekStart, today);
        long delMonth = deliveries.countByDeliveryPersonIdAndStatusAndDateBetween(rider, "delivered", monthStart, today.withDayOfMonth(today.lengthOfMonth()));
        long failedMonth = deliveries.countByDeliveryPersonIdAndStatusAndDateBetween(rider, "failed", monthStart, today.withDayOfMonth(today.lengthOfMonth()));

        return Map.of(
                "deliveredToday", delToday,
                "deliveredThisWeek", delWeek,
                "deliveredThisMonth", delMonth,
                "failedThisMonth", failedMonth
        );
    }
}
