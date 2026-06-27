package com.yowyob.notification.application.service;

import com.yowyob.notification.application.port.in.NotificationUseCase;
import com.yowyob.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Application service implementing the notification use case.
 * Delegates to the NotificationService infrastructure component.
 */
@Service
@RequiredArgsConstructor
public class NotificationApplicationService implements NotificationUseCase {

    private final NotificationService notificationService;

    @Override
    public void sendNotification(String recipient, String subject, String message) {
        notificationService.sendNotification(recipient, subject, message);
    }
}
