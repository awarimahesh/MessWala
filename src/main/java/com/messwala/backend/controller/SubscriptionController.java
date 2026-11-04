package com.messwala.backend.controller;

import com.messwala.backend.dto.SubscriptionRequest;
import com.messwala.backend.dto.SubscriptionResponse;
import com.messwala.backend.service.SubscriptionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    public ResponseEntity<SubscriptionResponse> subscribe(@Valid @RequestBody SubscriptionRequest request,
                                                          Authentication authentication) {
        SubscriptionResponse response = subscriptionService.subscribe(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SubscriptionResponse>> mySubscriptions(Authentication authentication) {
        return ResponseEntity.ok(subscriptionService.mySubscriptions(authentication.getName()));
    }

    @PutMapping("/pause")
    public ResponseEntity<SubscriptionResponse> pause(Authentication authentication) {
        return ResponseEntity.ok(subscriptionService.pause(authentication.getName()));
    }

    @PutMapping("/resume")
    public ResponseEntity<SubscriptionResponse> resume(Authentication authentication) {
        return ResponseEntity.ok(subscriptionService.resume(authentication.getName()));
    }
}