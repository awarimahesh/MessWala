package com.messwala.backend.controller;

import com.messwala.backend.dto.DeliveryResponse;
import com.messwala.backend.service.DeliveryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    public DeliveryController(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @GetMapping("/today")
    public ResponseEntity<List<DeliveryResponse>> today(
            @RequestParam(required = false) String area) {
        return ResponseEntity.ok(deliveryService.todaysList(area));
    }

    @PutMapping("/{id}/delivered")
    public ResponseEntity<DeliveryResponse> markDelivered(@PathVariable Long id,
                                                          Authentication authentication) {
        return ResponseEntity.ok(deliveryService.markDelivered(id, authentication.getName()));
    }
}