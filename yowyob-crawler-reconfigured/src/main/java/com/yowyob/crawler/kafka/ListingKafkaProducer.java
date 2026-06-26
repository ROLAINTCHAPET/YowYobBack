package com.yowyob.crawler.kafka;

import com.yowyob.crawler.dto.ListingEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class ListingKafkaProducer {

    @Value("${kafka.topics.listings}")
    private String topic;

    private final KafkaTemplate<String, ListingEvent> kafkaTemplate;

    public void publish(ListingEvent event) {
        kafkaTemplate.send(topic, event.getOsmId(), event)
            .whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("Échec Kafka pour {} : {}",
                        event.getName(), ex.getMessage());
                } else {
                    log.debug("Publié : {}", event.getName());
                }
            });
    }

    public void publishAll(List<ListingEvent> events) {
        events.forEach(this::publish);
        log.info("{} événements envoyés vers '{}'", events.size(), topic);
    }
}
