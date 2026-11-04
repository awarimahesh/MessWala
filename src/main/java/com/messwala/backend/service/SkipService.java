package com.messwala.backend.service;

import com.messwala.backend.dto.SkipRequest;
import com.messwala.backend.dto.SkipResponse;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Skip;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.SkipRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import com.messwala.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class SkipService {

    private final SkipRepository skipRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final Clock clock;
    private final LocalTime cutoffTime;

    public SkipService(SkipRepository skipRepository,
                       SubscriptionRepository subscriptionRepository,
                       UserRepository userRepository,
                       Clock clock,
                       @Value("${messwala.skip-cutoff:21:00}") String cutoff) {
        this.skipRepository = skipRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.userRepository = userRepository;
        this.clock = clock;
        this.cutoffTime = LocalTime.parse(cutoff);
    }

    @Transactional
    public SkipResponse skip(SkipRequest request, String customerEmail) {
        User customer = findCustomer(customerEmail);

        Subscription subscription = subscriptionRepository
                .findFirstByCustomerIdAndStatusIn(customer.getId(),
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found"));

        if (subscription.getStatus() == SubscriptionStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Your subscription is paused. Resume it before skipping meals");
        }

        MealType meal = request.mealType();
        if (meal == MealType.BOTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Meal type must be LUNCH or DINNER");
        }

        MealType planMeal = subscription.getPlan().getMealType();
        if (planMeal != MealType.BOTH && planMeal != meal) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Your plan does not include " + meal);
        }

        LocalDate date = request.date();
        if (date.isBefore(subscription.getStartDate())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Date is before your subscription start date");
        }

        LocalDateTime deadline = date.minusDays(1).atTime(cutoffTime);
        LocalDateTime now = LocalDateTime.now(clock);
        if (!now.isBefore(deadline)) {
            String time = cutoffTime.format(DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH));
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Skip cutoff passed. A meal on " + date + " must be skipped before "
                            + time + " on " + date.minusDays(1));
        }

        if (skipRepository.existsBySubscriptionIdAndDateAndMealType(subscription.getId(), date, meal)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already skipped this meal");
        }

        Skip skip = new Skip();
        skip.setSubscription(subscription);
        skip.setDate(date);
        skip.setMealType(meal);

        return toResponse(skipRepository.save(skip));
    }

    @Transactional(readOnly = true)
    public List<SkipResponse> mySkips(String customerEmail) {
        User customer = findCustomer(customerEmail);

        return subscriptionRepository
                .findFirstByCustomerIdAndStatusIn(customer.getId(),
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED))
                .map(sub -> skipRepository.findBySubscriptionIdOrderByDateDesc(sub.getId())
                        .stream()
                        .map(this::toResponse)
                        .toList())
                .orElse(List.of());
    }

    private User findCustomer(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private SkipResponse toResponse(Skip skip) {
        return new SkipResponse(skip.getId(), skip.getSubscription().getId(),
                skip.getDate(), skip.getMealType());
    }
}