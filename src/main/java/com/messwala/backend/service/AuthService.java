package com.messwala.backend.service;

import com.messwala.backend.dto.AuthResponse;
import com.messwala.backend.dto.LoginRequest;
import com.messwala.backend.dto.RegisterRequest;
import com.messwala.backend.entity.Role;
import com.messwala.backend.entity.User;
import com.messwala.backend.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponse register(RegisterRequest request) {
        String email = request.email().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        if (userRepository.existsByPhone(request.phone())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Phone already registered");
        }

        User user = new User();
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : Role.CUSTOMER);
        user.setArea(request.area());

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public AuthResponse login(LoginRequest request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        return toResponse(user);
    }

    private AuthResponse toResponse(User user) {
        return new AuthResponse(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}