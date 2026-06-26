package com.yowyob.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.yowyob.auth.dto.AuthResponse;
import com.yowyob.auth.dto.GoogleLoginRequest;
import com.yowyob.auth.dto.LoginRequest;
import com.yowyob.auth.dto.RegisterRequest;
import com.yowyob.auth.entity.User;
import com.yowyob.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final AuthenticationManager authenticationManager;
        private final GoogleAuthVerifier googleAuthVerifier;
        private final KafkaProducerService kafkaProducerService;

        @Transactional
        public AuthResponse register(RegisterRequest request) {
                String email = request.getEmail().trim().toLowerCase();
                log.info("Starting registration process for email: {}", email);
                if (userRepository.existsByEmail(email)) {
                        log.warn("Registration failed: Email already exists: {}", email);
                        throw new RuntimeException("Email already exists");
                }

                User user = User.builder()
                                .name(request.getName())
                                .email(email) // FIXED: save normalized email
                                .password(passwordEncoder.encode(request.getPassword()))
                                .role(User.Role.USER)
                                .status(User.Status.ACTIVE)
                                .emailVerified(false)
                                .build();

                user = userRepository.save(user);
                log.info("User saved with ID: {}", user.getId());

                // Envoyer l'événement Kafka pour indexation immédiate
                try {
                        com.yowyob.auth.event.UserCreatedEvent event = com.yowyob.auth.event.UserCreatedEvent.builder()
                                        .id(user.getId().toString())
                                        .email(user.getEmail())
                                        .username(user.getName())
                                        .firstName(user.getName())
                                        .build();
                        kafkaProducerService.sendUserCreatedEvent(event);
                } catch (Exception e) {
                        log.error("Failed to send Kafka event for user {}: {}", user.getEmail(), e.getMessage());
                }

                return generateAuthResponse(user, "User registered successfully");
        }

        public AuthResponse login(LoginRequest request) {
                String email = request.getEmail().trim().toLowerCase();
                log.info("Login request received for email: {}", email);
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> {
                                        log.warn("Login failed: User not found with email: {}", email);
                                        return new RuntimeException("Invalid credentials");
                                });

                if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                        log.warn("Login failed: Password mismatch for email: {}", email);
                        throw new RuntimeException("Invalid credentials");
                }

                if (user.getStatus() != User.Status.ACTIVE) {
                        throw new RuntimeException("Account is not active");
                }

                return generateAuthResponse(user, "Login successful");
        }

        @Transactional
        public AuthResponse googleLogin(GoogleLoginRequest request) {
                try {
                        GoogleIdToken.Payload payload = googleAuthVerifier.verify(request.getToken());
                        String email = payload.getEmail().trim().toLowerCase(); // FIXED: normalize google email
                        String name = (String) payload.get("name");

                        log.info("Google Login forized email: {}", email);

                        Optional<User> userOptional = userRepository.findByEmail(email);
                        User user;

                        if (userOptional.isPresent()) {
                                user = userOptional.get();
                                log.info("Existing user found for Google email: {}", email);
                        } else {
                                // Auto-register
                                log.info("New User from Google ({}). Registering...", email);
                                user = User.builder()
                                                .name(name != null ? name : email)
                                                .email(email) // FIXED: save normalized email
                                                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                                                .role(User.Role.USER)
                                                .status(User.Status.ACTIVE)
                                                .emailVerified(true) // Verified by Google
                                                .build();
                                user = userRepository.save(user);
                                log.info("User auto-registered via Google: {}", user.getEmail());

                                // Send Kafka Event for new Google user
                                try {
                                        com.yowyob.auth.event.UserCreatedEvent event = com.yowyob.auth.event.UserCreatedEvent
                                                        .builder()
                                                        .id(user.getId().toString())
                                                        .email(user.getEmail())
                                                        .username(user.getName())
                                                        .firstName(user.getName())
                                                        .build();
                                        kafkaProducerService.sendUserCreatedEvent(event);
                                        log.info("Kafka event sent for Google user: {}", user.getEmail());
                                } catch (Exception e) {
                                        log.error("Failed to send Kafka event for Google user {}: {}", user.getEmail(),
                                                        e.getMessage());
                                }
                        }

                        if (user.getStatus() != User.Status.ACTIVE) {
                                throw new RuntimeException("Account is not active");
                        }

                        return generateAuthResponse(user, "Google Login successful");

                } catch (Exception e) {
                        log.error("Google Login failed", e);
                        throw new RuntimeException("Google Login failed: " + e.getMessage());
                }
        }

        private AuthResponse generateAuthResponse(User user, String message) {
                String accessToken = jwtService.generateAccessToken(
                                user.getId(),
                                user.getEmail(),
                                user.getRole().name());

                String refreshToken = jwtService.generateRefreshToken(user.getId());

                return AuthResponse.builder()
                                .success(true)
                                .message(message)
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .expiresIn(jwtService.getExpirationTime())
                                .user(AuthResponse.UserDto.builder()
                                                .id(user.getId().toString())
                                                .name(user.getName())
                                                .email(user.getEmail())
                                                .role(user.getRole().name())
                                                .build())
                                .build();
        }
}
