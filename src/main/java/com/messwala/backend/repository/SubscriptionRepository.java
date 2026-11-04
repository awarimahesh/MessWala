package com.messwala.backend.repository;

import com.messwala.backend.entity.Subscription;
import com.messwala.backend.entity.SubscriptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    List<Subscription> findByCustomerId(Long customerId);

    List<Subscription> findByStatus(SubscriptionStatus status);

    boolean existsByCustomerIdAndStatus(Long customerId, SubscriptionStatus status);

    Optional<Subscription> findFirstByCustomerIdAndStatusIn(Long customerId, Collection<SubscriptionStatus> statuses);
}