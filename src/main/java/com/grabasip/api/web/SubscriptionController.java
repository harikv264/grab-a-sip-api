package com.grabasip.api.web;

import com.grabasip.api.domain.Subscription;
import com.grabasip.api.domain.SubscriptionPause;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.SubscriptionPauseRepository;
import com.grabasip.api.repo.SubscriptionRepository;
import com.grabasip.api.security.AdminGuard;
import com.grabasip.api.service.PlanCatalog;
import com.grabasip.api.web.dto.PauseRequest;
import com.grabasip.api.web.dto.SubscriptionRequest;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private static final Set<String> STATUSES = Set.of("active", "paused", "cancelled");
    private static final int MAX_PAUSE_DAYS = 5;

    private final SubscriptionRepository subs;
    private final SubscriptionPauseRepository pauses;
    private final CustomerRepository customers;
    private final PlanCatalog plans;
    private final AdminGuard admin;

    public SubscriptionController(SubscriptionRepository subs, SubscriptionPauseRepository pauses,
                                  CustomerRepository customers, PlanCatalog plans, AdminGuard admin) {
        this.subs = subs;
        this.pauses = pauses;
        this.customers = customers;
        this.plans = plans;
        this.admin = admin;
    }

    @GetMapping
    public List<Subscription> list(@RequestParam String token,
                                   @RequestParam(required = false) String status,
                                   @RequestParam(required = false) UUID customerId) {
        admin.require(token);
        if (customerId != null) return subs.findByCustomerIdOrderByCreatedAtDesc(customerId);
        if (status != null && !status.isBlank())
            return subs.findByStatusOrderByCreatedAtDesc(status.trim());
        return subs.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public Subscription get(@RequestParam String token, @PathVariable UUID id) {
        admin.require(token);
        return find(id);
    }

    @GetMapping("/{id}/pauses")
    public List<SubscriptionPause> pauseLog(@RequestParam String token, @PathVariable UUID id) {
        admin.require(token);
        return pauses.findBySubscriptionIdOrderByCreatedAtDesc(id);
    }

    @PostMapping
    public ResponseEntity<Subscription> create(@RequestParam String token,
                                               @RequestBody SubscriptionRequest body) {
        admin.require(token);
        if (body.customerId() == null)
            throw badRequest("customerId is required");
        if (customers.findById(body.customerId()).isEmpty())
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found");

        PlanCatalog.Plan plan = plans.get(body.planCode());
        if (plan == null) throw badRequest("Unknown plan");

        Subscription s = new Subscription();
        s.setCustomerId(body.customerId());
        s.setPlanCode(plan.code());
        s.setPlanName(plan.name());
        s.setPrice(plan.price());
        s.setStartDate(body.startDate() != null ? body.startDate() : LocalDate.now());
        s.setStatus(validStatus(body.status(), "active"));
        s.setNotes(trimOrNull(body.notes()));
        return ResponseEntity.status(HttpStatus.CREATED).body(subs.save(s));
    }

    @PutMapping("/{id}")
    public Subscription update(@RequestParam String token, @PathVariable UUID id,
                               @RequestBody SubscriptionRequest body) {
        admin.require(token);
        Subscription s = find(id);
        if (body.planCode() != null && !body.planCode().isBlank()) {
            PlanCatalog.Plan plan = plans.get(body.planCode());
            if (plan == null) throw badRequest("Unknown plan");
            s.setPlanCode(plan.code());
            s.setPlanName(plan.name());
            s.setPrice(plan.price());
        }
        if (body.startDate() != null) s.setStartDate(body.startDate());
        if (body.status() != null && !body.status().isBlank())
            s.setStatus(validStatus(body.status(), s.getStatus()));
        if (body.notes() != null) s.setNotes(trimOrNull(body.notes()));
        return subs.save(s);
    }

    @PostMapping("/{id}/pause")
    public Subscription pause(@RequestParam String token, @PathVariable UUID id,
                             @RequestBody PauseRequest body) {
        admin.require(token);
        Subscription s = find(id);
        int days = body.days() == null ? 0 : body.days();
        if (days < 1) throw badRequest("Enter at least 1 pause day");
        if (s.getPauseDaysUsed() + days > MAX_PAUSE_DAYS) {
            throw badRequest("That exceeds the 5 pause days allowed this month (used "
                    + s.getPauseDaysUsed() + ")");
        }

        SubscriptionPause p = new SubscriptionPause();
        p.setSubscriptionId(s.getId());
        p.setDays(days);
        p.setStartDate(body.startDate());
        p.setNote(trimOrNull(body.note()));
        pauses.save(p);

        s.setPauseDaysUsed(s.getPauseDaysUsed() + days);
        s.setStatus("paused");
        return subs.save(s);
    }

    // ── helpers ──────────────────────────────────────────────────
    private Subscription find(UUID id) {
        return subs.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }

    private static String validStatus(String status, String fallback) {
        if (status == null || status.isBlank()) return fallback;
        String s = status.trim().toLowerCase();
        if (!STATUSES.contains(s)) throw badRequest("Invalid status");
        return s;
    }

    private static String trimOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    private static ResponseStatusException badRequest(String msg) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, msg);
    }
}
