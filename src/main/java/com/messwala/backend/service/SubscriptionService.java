package com.messwala.backend.service;

import com.messwala.backend.dto.SubscriptionRequest;
import com.messwala.backend.dto.SubscriptionResponse;
import com.messwala.backend.entity.Plan;
import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.PlanRepository;
import com.messwala.backend.repository.SubscriptionRepository;
import com.messwala.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final UserRepository userRepository;

    public SubscriptionService(SubscriptionRepository subscriptionRepository,
                               PlanRepository planRepository,
                               UserRepository userRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public SubscriptionResponse subscribe(SubscriptionRequest request, String customerEmail) {
        User customer = findCustomer(customerEmail);

        Plan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));

        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Plan is not available");
        }

        boolean alreadySubscribed =
                subscriptionRepository.existsByCustomerIdAndStatus(customer.getId(), SubscriptionStatus.ACTIVE)
                        || subscriptionRepository.existsByCustomerIdAndStatus(customer.getId(), SubscriptionStatus.PAUSED);
        if (alreadySubscribed) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already have a subscription");
        }

        LocalDate startDate = request.startDate() != null ? request.startDate() : LocalDate.now();
        if (startDate.isBefore(LocalDate.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date cannot be in the past");
        }

        Subscription subscription = new Subscription();
        subscription.setCustomer(customer);
        subscription.setPlan(plan);
        subscription.setStartDate(startDate);
        subscription.setStatus(SubscriptionStatus.ACTIVE);

        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional(readOnly = true)
    public List<SubscriptionResponse> mySubscriptions(String customerEmail) {
        User customer = findCustomer(customerEmail);

        return subscriptionRepository.findByCustomerId(customer.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public SubscriptionResponse pause(String customerEmail) {
        Subscription subscription = findCurrent(customerEmail);

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not active");
        }

        subscription.setStatus(SubscriptionStatus.PAUSED);
        return toResponse(subscriptionRepository.save(subscription));
    }

    @Transactional
    public SubscriptionResponse resume(String customerEmail) {
        Subscription subscription = findCurrent(customerEmail);

        if (subscription.getStatus() != SubscriptionStatus.PAUSED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is not paused");
        }

        subscription.setStatus(SubscriptionStatus.ACTIVE);
        return toResponse(subscriptionRepository.save(subscription));
    }

    private Subscription findCurrent(String customerEmail) {
        User customer = findCustomer(customerEmail);
        return subscriptionRepository
                .findFirstByCustomerIdAndStatusIn(customer.getId(),
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.PAUSED))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No active subscription found"));
    }

    private User findCustomer(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        Plan plan = s.getPlan();
        return new SubscriptionResponse(s.getId(), plan.getId(), plan.getName(), plan.getMealType(),
                plan.getPricePerMonth(), s.getStartDate(), s.getEndDate(), s.getStatus());
    }
}