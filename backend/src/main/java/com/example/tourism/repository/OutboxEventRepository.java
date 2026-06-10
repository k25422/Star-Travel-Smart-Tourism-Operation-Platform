package com.example.tourism.repository;

import com.example.tourism.domain.OutboxEvent;
import com.example.tourism.domain.OutboxEventStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxEventRepository extends JpaRepository<OutboxEvent, Long> {
    List<OutboxEvent> findTop50ByStatusAndNextRetryAtBeforeOrderByCreatedAtAsc(OutboxEventStatus status, LocalDateTime nextRetryAt);
}
