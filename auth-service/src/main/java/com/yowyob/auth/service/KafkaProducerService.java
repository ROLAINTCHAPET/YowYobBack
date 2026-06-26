package com.yowyob.auth.service;

import com.yowyob.auth.event.UserCreatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaProducerService {

    private final KafkaTemplate<String, UserCreatedEvent> kafkaTemplate;
    private static final String TOPIC = "user.events";

    @org.springframework.scheduling.annotation.Async
    public void sendUserCreatedEvent(UserCreatedEvent event) {
        log.info("Sending UserCreatedEvent to Kafka: {}", event);
        try {
            kafkaTemplate.send(TOPIC, event.getId(), event);
            log.info("Successfully sent event for user: {}", event.getEmail());
        } catch (Exception e) {
            log.error("Failed to send event to Kafka: {}", e.getMessage(), e);
        }
    }
}
