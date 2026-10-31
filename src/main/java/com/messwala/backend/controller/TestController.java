package com.messwala.backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/api/me")
    public String me(Authentication authentication) {
        return "Logged in as " + authentication.getName() + " with " + authentication.getAuthorities();
    }

    @GetMapping("/api/owner/ping")
    public String ownerPing() {
        return "Hello Owner";
    }
}