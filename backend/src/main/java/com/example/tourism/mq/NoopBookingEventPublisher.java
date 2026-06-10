package com.example.tourism.mq;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class NoopBookingEventPublisher implements BookingEventPublisher {

    @Override
    public void publishBookingCreated(BookingCreatedEvent event) {
        // Kafka is optional for local development. This no-op keeps the normal app startup simple.
    }
}
