package com.example.tourism.mq;

import com.example.tourism.domain.OutboxEvent;
import com.example.tourism.domain.OutboxEventStatus;
import com.example.tourism.repository.OutboxEventRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxEventRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxEventRelay.class);

    private final OutboxEventRepository outboxEventRepository;
    private final BookingEventPublisher bookingEventPublisher;
    private final ObjectMapper objectMapper;

    public OutboxEventRelay(OutboxEventRepository outboxEventRepository,
                            BookingEventPublisher bookingEventPublisher,
                            ObjectMapper objectMapper) {
        this.outboxEventRepository = outboxEventRepository;
        this.bookingEventPublisher = bookingEventPublisher;
        this.objectMapper = objectMapper;
    }

    @Transactional
    @Scheduled(fixedDelayString = "${app.outbox.fixed-delay-ms:5000}")
    public void publishPendingEvents() {
        List<OutboxEvent> events = outboxEventRepository.findTop50ByStatusAndNextRetryAtBeforeOrderByCreatedAtAsc(
                OutboxEventStatus.PENDING,
                LocalDateTime.now()
        );

        for (OutboxEvent event : events) {
            try {
                if ("BOOKING_CREATED".equals(event.getEventType())) {
                    BookingCreatedEvent payload = objectMapper.readValue(event.getPayload(), BookingCreatedEvent.class);
                    bookingEventPublisher.publishBookingCreated(payload);
                }
                event.setStatus(OutboxEventStatus.PUBLISHED);
                event.setPublishedAt(LocalDateTime.now());
                event.setLastError(null);
            } catch (Exception ex) {
                event.setStatus(OutboxEventStatus.PENDING);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setNextRetryAt(LocalDateTime.now().plusSeconds(Math.min(60, 5L * event.getRetryCount())));
                event.setLastError(ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage());
                log.warn("Failed to publish outbox event id={}, retryCount={}", event.getId(), event.getRetryCount(), ex);
            }
        }
    }
}
