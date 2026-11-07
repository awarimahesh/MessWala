package com.messwala.backend.service;

import com.messwala.backend.dto.DeliveryResponse;
import com.messwala.backend.entity.Delivery;
import com.messwala.backend.entity.DeliveryStatus;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.DeliveryRepository;
import com.messwala.backend.repository.SkipRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class DeliveryService {

    private final SubscriptionRepository subscriptionRepository;
    private final SkipRepository skipRepository;
    private final DeliveryRepository deliveryRepository;

    public DeliveryService(SubscriptionRepository subscriptionRepository,
                           SkipRepository skipRepository,
                           DeliveryRepository deliveryRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.skipRepository = skipRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public int generateFor(LocalDate date) {
        List<Subscription> active = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        int created = 0;

        for (MealType meal : List.of(MealType.LUNCH, MealType.DINNER)) {
            Set<Long> skippedIds = skipRepository.findByDateAndMealType(date, meal).stream()
                    .map(skip -> skip.getSubscription().getId())
                    .collect(Collectors.toSet());

            for (Subscription subscription : active) {
                if (!covers(subscription, meal, date)) {
                    continue;
                }
                if (skippedIds.contains(subscription.getId())) {
                    continue;
                }
                if (deliveryRepository.existsBySubscriptionIdAndDateAndMealType(
                        subscription.getId(), date, meal)) {
                    continue;
                }

                Delivery delivery = new Delivery();
                delivery.setSubscription(subscription);
                delivery.setDate(date);
                delivery.setMealType(meal);
                delivery.setStatus(DeliveryStatus.PENDING);
                deliveryRepository.save(delivery);
                created++;
            }
        }

        return created;
    }

    @Transactional(readOnly = true)
    public List<DeliveryResponse> listByDate(LocalDate date) {
        return deliveryRepository.findByDate(date).stream()
                .sorted(Comparator.comparing(Delivery::getMealType).thenComparing(Delivery::getId))
                .map(this::toResponse)
                .toList();
    }

    private boolean covers(Subscription subscription, MealType meal, LocalDate date) {
        MealType planMeal = subscription.getPlan().getMealType();
        boolean mealMatches = planMeal == meal || planMeal == MealType.BOTH;
        boolean started = !subscription.getStartDate().isAfter(date);
        boolean notEnded = subscription.getEndDate() == null || !subscription.getEndDate().isBefore(date);
        return mealMatches && started && notEnded;
    }

    private DeliveryResponse toResponse(Delivery delivery) {
        User customer = delivery.getSubscription().getCustomer();
        return new DeliveryResponse(delivery.getId(), delivery.getDate(), delivery.getMealType(),
                delivery.getStatus(), customer.getName(), customer.getPhone(), customer.getArea());
    }
}