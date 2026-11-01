package com.messwala.backend.repository;

import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface MenuRepository extends JpaRepository<Menu, Long> {

    List<Menu> findByDate(LocalDate date);

    boolean existsByDateAndMealType(LocalDate date, MealType mealType);
}