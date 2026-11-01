package com.messwala.backend.service;

import com.messwala.backend.dto.MenuRequest;
import com.messwala.backend.dto.MenuResponse;
import com.messwala.backend.entity.MealType;
import com.messwala.backend.entity.Menu;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.MenuRepository;
import com.messwala.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class MenuService {

    private final MenuRepository menuRepository;
    private final UserRepository userRepository;

    public MenuService(MenuRepository menuRepository, UserRepository userRepository) {
        this.menuRepository = menuRepository;
        this.userRepository = userRepository;
    }

    public MenuResponse create(MenuRequest request, String ownerEmail) {
        if (request.mealType() == MealType.BOTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Menu meal type must be LUNCH or DINNER");
        }

        if (menuRepository.existsByDateAndMealType(request.date(), request.mealType())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Menu already posted for this date and meal");
        }

        User owner = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        Menu menu = new Menu();
        menu.setDate(request.date());
        menu.setMealType(request.mealType());
        menu.setItems(request.items().trim());
        menu.setCreatedBy(owner);

        return toResponse(menuRepository.save(menu));
    }

    public List<MenuResponse> getByDate(LocalDate date) {
        return menuRepository.findByDate(date)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private MenuResponse toResponse(Menu menu) {
        return new MenuResponse(menu.getId(), menu.getDate(), menu.getMealType(), menu.getItems());
    }
}