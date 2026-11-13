package com.messwala.repository;

import com.messwala.entity.Bill;
import com.messwala.entity.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BillRepository extends JpaRepository<Bill, Long> {

    List<Bill> findByPaymentStatus(PaymentStatus paymentStatus);

    List<Bill> findByPaymentStatusAndMonth(PaymentStatus paymentStatus, String month);
}