package com.messwala.backend.repository;

import com.messwala.backend.entity.Bill;
import com.messwala.backend.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findBySubscriptionCustomerId(Long customerId);

    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);
}