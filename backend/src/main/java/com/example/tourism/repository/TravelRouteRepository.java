package com.example.tourism.repository;

import com.example.tourism.domain.TravelRoute;
import java.util.Optional;
import javax.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// 旅游路线数据库访问层。
public interface TravelRouteRepository extends JpaRepository<TravelRoute, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from TravelRoute r where r.id = :id")
    Optional<TravelRoute> findByIdForUpdate(@Param("id") Long id);

    @Modifying
    @Query("update TravelRoute r set r.version = 0 where r.version is null")
    int initializeMissingVersions();
}
