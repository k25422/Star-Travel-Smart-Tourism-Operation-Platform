package com.example.tourism.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class BookingEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(BookingEventConsumer.class);

    @KafkaListener(topics = "${app.kafka.booking-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleBookingCreated(BookingCreatedEvent event) {
        log.info("Booking created event consumed: bookingId={}, route={}, traveler={}, amount={}",
                event.getBookingId(), event.getRouteTitle(), event.getTravelerName(), event.getTotalAmount());
    }
}