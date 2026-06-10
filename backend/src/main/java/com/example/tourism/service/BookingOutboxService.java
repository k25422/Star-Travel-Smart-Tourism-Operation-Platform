package com.example.tourism.service;

import com.example.tourism.domain.Booking;
import com.example.tourism.domain.OutboxEvent;
import com.example.tourism.domain.OutboxEventStatus;
import com.example.tourism.mq.BookingCreatedEvent;
import com.example.tourism.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;

@Service
public class BookingOutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    public BookingOutboxService(OutboxEventRepository outboxEventRepository, ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.objectMapper = objectMapper;
    }

    public void enqueueBookingCreated(Booking booking) {
        BookingCreatedEvent event = new BookingCreatedEvent(
                booking.getId(),
                booking.getRoute().getId(),
                booking.getRoute().getTitle(),
                booking.getTravelerName(),
                booking.getPhone(),
                booking.getTravelers(),
                booking.getTotalAmount()
        );

        OutboxEvent outboxEvent = new OutboxEvent();
        outboxEvent.setAggregateType("BOOKING");
        outboxEvent.setAggregateId(booking.getId());
        outboxEvent.setEventType("BOOKING_CREATED");
        outboxEvent.setStatus(OutboxEventStatus.PENDING);
        outboxEvent.setPayload(writePayload(event));
        outboxEventRepository.save(outboxEvent);
    }

    private String writePayload(BookingCreatedEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException("Failed to serialize booking outbox event", ex);
        }
    }
}
