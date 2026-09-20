package com.grabasip.api.web;

import com.grabasip.api.domain.AppUser;
import com.grabasip.api.domain.Customer;
import com.grabasip.api.domain.Delivery;
import com.grabasip.api.domain.Subscription;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.DeliveryRepository;
import com.grabasip.api.repo.SubscriptionRepository;
import com.grabasip.api.security.CurrentUserService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** A customer's own view — only their subscriptions & deliveries. */
@RestController
@RequestMapping("/api/customer")
public class CustomerSelfController {

    private final CustomerRepository customers;
    private final SubscriptionRepository subscriptions;
    private final DeliveryRepository deliveries;
    private final CurrentUserService currentUser;

    public CustomerSelfController(CustomerRepository customers, SubscriptionRepository subscriptions,
                                 DeliveryRepository deliveries, CurrentUserService currentUser) {
        this.customers = customers;
        this.subscriptions = subscriptions;
        this.deliveries = deliveries;
        this.currentUser = currentUser;
    }

    private UUID customerId() {
        AppUser u = currentUser.current()
                .filter(a -> "customer".equalsIgnoreCase(a.getRole()))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.FORBIDDEN, "Customers only"));
        if (u.getCustomerId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Customer account isn't linked yet");
        }
        return u.getCustomerId();
    }

    @GetMapping("/profile")
    public Customer profile() {
        return customers.findById(customerId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    @GetMapping("/subscriptions")
    public List<Subscription> mySubscriptions() {
        return subscriptions.findByCustomerIdOrderByCreatedAtDesc(customerId());
    }

    @GetMapping("/deliveries")
    public List<Delivery> myDeliveries() {
        return deliveries.findByCustomerIdOrderByDateDesc(customerId());
    }

    /** GET /api/customer/summary — quick figures for the home screen. */
    @GetMapping("/summary")
    public Map<String, Object> summary() {
        UUID id = customerId();
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDate monthEnd = today.withDayOfMonth(today.lengthOfMonth());

        long activeSubs = subscriptions.findByCustomerIdOrderByCreatedAtDesc(id).stream()
                .filter(s -> "active".equals(s.getStatus())).count();
        long deliveredThisMonth = deliveries.countByCustomerIdAndStatusAndDateBetween(id, "delivered", monthStart, monthEnd);

        // Next upcoming (today onward, not yet delivered/failed).
        LocalDate next = deliveries.findByCustomerIdOrderByDateDesc(id).stream()
                .filter(d -> !d.getDate().isBefore(today))
                .filter(d -> "pending".equals(d.getStatus()) || "dispatched".equals(d.getStatus()))
                .map(Delivery::getDate)
                .min(LocalDate::compareTo)
                .orElse(null);

        Map<String, Object> out = new HashMap<>();
        out.put("activeSubscriptions", activeSubs);
        out.put("deliveredThisMonth", deliveredThisMonth);
        out.put("nextDeliveryDate", next);
        return out;
    }
}
