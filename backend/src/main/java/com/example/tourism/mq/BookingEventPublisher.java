package com.example.tourism.mq;

public interface BookingEventPublisher {

    void publishBookingCreated(BookingCreatedEvent event);
}
