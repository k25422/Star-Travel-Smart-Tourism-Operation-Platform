package com.example.tourism.repository;

import com.example.tourism.domain.Destination;
import org.springframework.data.jpa.repository.JpaRepository;

// 目的地数据库访问层。
// 继承 JpaRepository 后，自动拥有 findAll、findById、save、delete 等方法。
public interface DestinationRepository extends JpaRepository<Destination, Long> {
}
