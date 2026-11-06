package com.messwala.backend.service;

import com.messwala.backend.dto.CookingCountResponse;
import com.messwala.backend.dto.CookingCountResponse.MealCount;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import com.messwala.backend.repository.SkipRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class CookingCountService {

    private final SubscriptionRepository subscriptionRepository;
    private final SkipRepository skipRepository;

    public CookingCountService(SubscriptionRepository subscriptionRepository,
                               SkipRepository skipRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.skipRepository = skipRepository;
    }

    @Transactional(readOnly = true)
    public CookingCountResponse count(LocalDate date) {
        List<Subscription> active = subscriptionRepository.findByStatus(SubscriptionStatus.ACTIVE);
        List<Subscription> paused = subscriptionRepository.findByStatus(SubscriptionStatus.PAUSED);

        return new CookingCountResponse(
                date,
                countFor(MealType.LUNCH, date, active, paused),
                countFor(MealType.DINNER, date, active, paused));
    }

    private MealCount countFor(MealType meal, LocalDate date,
                               List<Subscription> active, List<Subscription> paused) {
        List<Subscription> activeForMeal = active.stream()
                .filter(s -> covers(s, meal, date))
                .toList();

        int pausedForMeal = (int) paused.stream()
                .filter(s -> covers(s, meal, date))
                .count();

        Set<Long> skippedIds = skipRepository.findByDateAndMealType(date, meal).stream()
                .map(skip -> skip.getSubscription().getId())
                .collect(Collectors.toSet());

        int skipped = (int) activeForMeal.stream()
                .filter(s -> skippedIds.contains(s.getId()))
                .count();

        return new MealCount(activeForMeal.size(), pausedForMeal, skipped, activeForMeal.size() - skipped);
    }

    private boolean covers(Subscription subscription, MealType meal, LocalDate date) {
        MealType planMeal = subscription.getPlan().getMealType();
        boolean mealMatches = planMeal == meal || planMeal == MealType.BOTH;
        boolean started = !subscription.getStartDate().isAfter(date);
        boolean notEnded = subscription.getEndDate() == null || !subscription.getEndDate().isBefore(date);
        return mealMatches && started && notEnded;
    }
}