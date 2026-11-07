package com.messwala.backend.controller;

import com.messwala.backend.dto.CookingCountResponse;
import com.messwala.backend.dto.DeliveryResponse;
import com.messwala.backend.service.CookingCountService;
import com.messwala.backend.service.DeliveryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final CookingCountService cookingCountService;
    private final DeliveryService deliveryService;

    public OwnerController(CookingCountService cookingCountService, DeliveryService deliveryService) {
        this.cookingCountService = cookingCountService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/cooking-count")
    public ResponseEntity<CookingCountResponse> cookingCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(cookingCountService.count(date));
    }

    @PostMapping("/deliveries/generate")
    public ResponseEntity<Map<String, Object>> generateDeliveries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        int created = deliveryService.generateFor(date);
        return ResponseEntity.ok(Map.of("date", date, "created", created));
    }

    @GetMapping("/deliveries")
    public ResponseEntity<List<DeliveryResponse>> deliveries(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(deliveryService.listByDate(date));
    }
}