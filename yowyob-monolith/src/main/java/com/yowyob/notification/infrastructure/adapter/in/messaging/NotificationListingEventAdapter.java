package com.yowyob.notification.infrastructure.adapter.in.messaging;

import com.yowyob.config.RabbitMQConfig;
import com.yowyob.listing.event.ListingEvent;
import com.yowyob.notification.application.port.in.NotificationUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * RabbitMQ messaging adapter for Notifications.
 * Consumes listing events and triggers notification sending via the use case port.
 */
@Component("notificationListingEventAdapter")
@Slf4j
@RequiredArgsConstructor
public class NotificationListingEventAdapter {

    private final NotificationUseCase notificationUseCase;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void handleListingEvent(ListingEvent event) {
        log.info("Received Listing Event: {}", event.getEventType());

        if ("CREATED".equals(event.getEventType())) {
            String subject = "New Listing Alert: " + event.getTitle();
            String message = String.format("A new listing '%s' has been posted in %s for %.2f FCFA.",
                    event.getTitle(), event.getCategory(), event.getPrice());

            notificationUseCase.sendNotification("users@yowyob.com", subject, message);
        }
    }
}
