package com.messwala.backend.repository;

import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Skip;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface SkipRepository extends JpaRepository<Skip, Long> {

    boolean existsBySubscriptionIdAndDateAndMealType(Long subscriptionId, LocalDate date, MealType mealType);

    long countByDateAndMealType(LocalDate date, MealType mealType);

    long countBySubscriptionIdAndDateBetween(Long subscriptionId, LocalDate from, LocalDate to);

    List<Skip> findBySubscriptionIdOrderByDateDesc(Long subscriptionId);

    List<Skip> findByDateAndMealType(LocalDate date, MealType mealType);
}