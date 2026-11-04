package com.messwala.backend.controller;

import com.messwala.backend.dto.SkipRequest;
import com.messwala.backend.dto.SkipResponse;
import com.messwala.backend.service.SkipService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/skips")
public class SkipController {

    private final SkipService skipService;

    public SkipController(SkipService skipService) {
        this.skipService = skipService;
    }

    @PostMapping
    public ResponseEntity<SkipResponse> skip(@Valid @RequestBody SkipRequest request,
                                             Authentication authentication) {
        SkipResponse response = skipService.skip(request, authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<SkipResponse>> mySkips(Authentication authentication) {
        return ResponseEntity.ok(skipService.mySkips(authentication.getName()));
    }
}