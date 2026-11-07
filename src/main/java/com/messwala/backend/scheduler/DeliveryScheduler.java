package com.messwala.backend.scheduler;

import com.messwala.backend.service.DeliveryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

@Component
public class DeliveryScheduler {

    private static final Logger log = LoggerFactory.getLogger(DeliveryScheduler.class);

    private final DeliveryService deliveryService;
    private final Clock clock;

    public DeliveryScheduler(DeliveryService deliveryService, Clock clock) {
        this.deliveryService = deliveryService;
        this.clock = clock;
    }

    @Scheduled(cron = "${messwala.delivery.cron:0 0 22 * * *}", zone = "Asia/Kolkata")
    public void generateTomorrowsList() {
        LocalDate tomorrow = LocalDate.now(clock).plusDays(1);
        int created = deliveryService.generateFor(tomorrow);
        log.info("Delivery list for {} generated: {} new deliveries", tomorrow, created);
    }
}