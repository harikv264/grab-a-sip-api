package com.grabasip.api.service;

import com.grabasip.api.domain.Customer;
import com.grabasip.api.domain.Delivery;
import com.grabasip.api.domain.Subscription;
import com.grabasip.api.repo.CustomerRepository;
import com.grabasip.api.repo.DeliveryRepository;
import com.grabasip.api.repo.HolidayRepository;
import com.grabasip.api.repo.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Generates the day's deliveries from active subscriptions (Mon–Sat, skipping holidays). */
@Service
public class DeliveryGenerationService {

    public record GenerateResult(int created, int skipped, String reason) {}

    private final SubscriptionRepository subs;
    private final CustomerRepository customers;
    private final DeliveryRepository deliveries;
    private final HolidayRepository holidays;

    public DeliveryGenerationService(SubscriptionRepository subs, CustomerRepository customers,
                                     DeliveryRepository deliveries, HolidayRepository holidays) {
        this.subs = subs;
        this.customers = customers;
        this.deliveries = deliveries;
        this.holidays = holidays;
    }

    public boolean isDeliveryDay(LocalDate date) {
        return date.getDayOfWeek() != DayOfWeek.SUNDAY && !holidays.existsByDate(date);
    }

    @Transactional
    public GenerateResult generateForDate(LocalDate date) {
        if (date.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return new GenerateResult(0, 0, "Sunday — we don't deliver");
        }
        if (holidays.existsByDate(date)) {
            return new GenerateResult(0, 0, "Public holiday — no deliveries");
        }

        List<Subscription> active = subs.findByStatusOrderByCreatedAtDesc("active");
        int created = 0;
        int skipped = 0;
        for (Subscription s : active) {
            if (deliveries.existsBySubscriptionIdAndDate(s.getId(), date)) {
                skipped++;
                continue;
            }
            Optional<Customer> cOpt = customers.findById(s.getCustomerId());
            if (cOpt.isEmpty()) {
                skipped++;
                continue;
            }
            Customer c = cOpt.get();
            Delivery d = new Delivery();
            d.setSubscriptionId(s.getId());
            d.setCustomerId(c.getId());
            d.setDate(date);
            d.setStatus("pending");
            d.setCustomerName(c.getName());
            d.setCustomerPhone(c.getPhone());
            d.setAddressText(composeAddress(c));
            d.setPlanName(s.getPlanName());
            deliveries.save(d);
            created++;
        }
        return new GenerateResult(created, skipped, null);
    }

    private static String composeAddress(Customer c) {
        List<String> parts = new ArrayList<>();
        if (notBlank(c.getFlatHouse())) parts.add(c.getFlatHouse().trim());
        if (notBlank(c.getStreet())) parts.add(c.getStreet().trim());
        if (notBlank(c.getLocality())) parts.add(c.getLocality().trim());
        if (notBlank(c.getPincode())) parts.add(c.getPincode().trim());
        String base = String.join(", ", parts);
        if (notBlank(c.getLandmark())) base += " (" + c.getLandmark().trim() + ")";
        return base;
    }

    private static boolean notBlank(String s) {
        return s != null && !s.trim().isEmpty();
    }
}
