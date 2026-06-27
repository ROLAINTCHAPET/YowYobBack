package com.yowyob.auth.infrastructure.adapter.in.web;

import com.yowyob.auth.application.port.in.AuthUseCase;
import com.yowyob.auth.domain.model.User;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.LoginResponse;
import com.yowyob.auth.dto.RegisterRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Endpoints for user registration and login")
public class AuthController {

    private final AuthUseCase authUseCase;

    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account with the provided details")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());
        User user = authUseCase.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates a user and returns JWT tokens")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authUseCase.login(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth Service (Hexagonal) is running!");
    }
}
