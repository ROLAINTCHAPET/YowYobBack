package com.yowyob.notification.application.port.in;

/**
 * Input port (use case) for notification operations.
 */
public interface NotificationUseCase {
    void sendNotification(String recipient, String subject, String message);
}
