package com.example.tourism.repository;

import com.example.tourism.domain.Booking;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

// Repository 是数据库访问层。
// JpaRepository<Booking, Long> 表示：操作 Booking 表，主键类型是 Long。
public interface BookingRepository extends JpaRepository<Booking, Long> {
    List<Booking> findAllByOrderByCreatedAtDesc();
    List<Booking> findByUserUsernameOrderByCreatedAtDesc(String username);
    List<Booking> findByUserIsNull();
    Optional<Booking> findByIdAndUserUsername(Long id, String username);
    Optional<Booking> findByUserUsernameAndClientRequestId(String username, String clientRequestId);
    long countByRouteId(Long routeId);
}
