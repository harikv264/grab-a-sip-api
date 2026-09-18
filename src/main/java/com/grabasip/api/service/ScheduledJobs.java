package com.grabasip.api.service;

import com.grabasip.api.domain.Subscription;
import com.grabasip.api.repo.SubscriptionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/** Background jobs. Times are in IST. */
@Component
public class ScheduledJobs {

    private static final Logger log = LoggerFactory.getLogger(ScheduledJobs.class);

    private final SubscriptionRepository subs;
    private final DeliveryGenerationService generation;

    public ScheduledJobs(SubscriptionRepository subs, DeliveryGenerationService generation) {
        this.subs = subs;
        this.generation = generation;
    }

    /** Reset each subscription's monthly pause allowance at the start of the month. */
    @Scheduled(cron = "0 5 0 1 * *", zone = "Asia/Kolkata")
    public void resetMonthlyPauses() {
        List<Subscription> all = subs.findAll();
        int reset = 0;
        for (Subscription s : all) {
            if (s.getPauseDaysUsed() != 0) {
                s.setPauseDaysUsed(0);
                reset++;
            }
        }
        if (reset > 0) subs.saveAll(all);
        log.info("Monthly pause reset: cleared {} subscription(s)", reset);
    }

    /** Generate the day's deliveries from active subscriptions each morning. */
    @Scheduled(cron = "0 0 5 * * *", zone = "Asia/Kolkata")
    public void generateTodaysDeliveries() {
        LocalDate today = LocalDate.now(java.time.ZoneId.of("Asia/Kolkata"));
        var result = generation.generateForDate(today);
        log.info("Auto-generate deliveries for {}: created={} skipped={} reason={}",
                today, result.created(), result.skipped(), result.reason());
    }
}
