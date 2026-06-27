package com.yowyob.notification.infrastructure.adapter.in.web;

import com.yowyob.notification.application.port.in.NotificationUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller adapter for notification endpoints (hexagonal architecture).
 * Delegates all operations to the NotificationUseCase input port.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Endpoints for managing and testing notifications")
public class NotificationControllerAdapter {

    private final NotificationUseCase notificationUseCase;

    @PostMapping("/test-email")
    @Operation(summary = "Test Email Sending", description = "Manually triggers a simulated email notification")
    public ResponseEntity<String> testEmail(
            @RequestParam String to,
            @RequestParam String subject,
            @RequestParam String body) {
        notificationUseCase.sendNotification(to, subject, body);
        return ResponseEntity.ok("Notification simulation triggered. Check logs.");
    }

    @GetMapping("/health")
    public String health() {
        return "Notification Service is running!";
    }
}
