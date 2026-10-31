package com.messwala.backend.service;

import com.messwala.backend.dto.PlanRequest;
import com.messwala.backend.dto.PlanResponse;
import com.messwala.backend.entity.Plan;
import com.messwala.backend.repository.PlanRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PlanService {

    private final PlanRepository planRepository;

    public PlanService(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    public PlanResponse create(PlanRequest request) {
        String name = request.name().trim();

        if (planRepository.existsByNameIgnoreCase(name)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Plan with this name already exists");
        }

        Plan plan = new Plan();
        plan.setName(name);
        plan.setMealType(request.mealType());
        plan.setPricePerMonth(request.pricePerMonth());
        plan.setActive(true);

        return toResponse(planRepository.save(plan));
    }

    public List<PlanResponse> listActive() {
        return planRepository.findByActiveTrue()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private PlanResponse toResponse(Plan plan) {
        return new PlanResponse(plan.getId(), plan.getName(), plan.getMealType(),
                plan.getPricePerMonth(), plan.isActive());
    }
}