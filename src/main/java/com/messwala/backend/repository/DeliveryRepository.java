package com.messwala.backend.repository;

import com.messwala.backend.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    List<Delivery> findByDeliveryPersonIdAndDate(Long deliveryPersonId, LocalDate date);

    List<Delivery> findByDate(LocalDate date);
}