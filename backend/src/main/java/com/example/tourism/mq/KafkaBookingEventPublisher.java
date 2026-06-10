package com.example.tourism.mq;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaBookingEventPublisher implements BookingEventPublisher {

    private final KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate;
    private final String bookingTopic;

    public KafkaBookingEventPublisher(KafkaTemplate<String, BookingCreatedEvent> kafkaTemplate,
                                      @Value("${app.kafka.booking-topic}") String bookingTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.bookingTopic = bookingTopic;
    }

    @Override
    public void publishBookingCreated(BookingCreatedEvent event) {
        kafkaTemplate.send(bookingTopic, String.valueOf(event.getBookingId()), event);
    }
}
