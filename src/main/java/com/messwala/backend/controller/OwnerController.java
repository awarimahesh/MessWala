package com.messwala.backend.controller;

import com.messwala.backend.dto.CookingCountResponse;
import com.messwala.backend.service.CookingCountService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/owner")
public class OwnerController {

    private final CookingCountService cookingCountService;

    public OwnerController(CookingCountService cookingCountService) {
        this.cookingCountService = cookingCountService;
    }

    @GetMapping("/cooking-count")
    public ResponseEntity<CookingCountResponse> cookingCount(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(cookingCountService.count(date));
    }
}