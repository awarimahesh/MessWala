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
import com.messwala.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
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
    private final UserRepository userRepository;
    private final Clock clock;

    public DeliveryService(SubscriptionRepository subscriptionRepository,
                           SkipRepository skipRepository,
                           DeliveryRepository deliveryRepository,
                           UserRepository userRepository,
                           Clock clock) {
        this.subscriptionRepository = subscriptionRepository;
        this.skipRepository = skipRepository;
        this.deliveryRepository = deliveryRepository;
        this.userRepository = userRepository;
        this.clock = clock;
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

    @Transactional(readOnly = true)
    public List<DeliveryResponse> todaysList(String area) {
        LocalDate today = LocalDate.now(clock);

        return deliveryRepository.findByDate(today).stream()
                .filter(d -> area == null || area.isBlank()
                        || area.equalsIgnoreCase(areaOf(d)))
                .sorted(Comparator.comparing(this::areaOf, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(Delivery::getMealType)
                        .thenComparing(Delivery::getId))
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public DeliveryResponse markDelivered(Long deliveryId, String staffEmail) {
        User staff = userRepository.findByEmail(staffEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Delivery not found"));

        if (!delivery.getDate().equals(LocalDate.now(clock))) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Only today's deliveries can be marked as delivered");
        }

        if (delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Delivery already marked as delivered");
        }

        delivery.setStatus(DeliveryStatus.DELIVERED);
        delivery.setDeliveryPerson(staff);

        return toResponse(deliveryRepository.save(delivery));
    }

    private String areaOf(Delivery delivery) {
        String area = delivery.getSubscription().getCustomer().getArea();
        return area == null ? "" : area;
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