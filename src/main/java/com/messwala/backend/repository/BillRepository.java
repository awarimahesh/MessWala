package com.messwala.backend.repository;
import com.messwala.backend.entity.Bill;
import com.messwala.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {


    Optional<Bill> findBySubscriptionIdAndMonth(Long subscriptionId, String month);

    List<Bill> findBySubscriptionCustomerId(Long customerId);

    // Day 15
    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Bill> findByPaymentStatusAndMonth(PaymentStatus paymentStatus, String month);
}