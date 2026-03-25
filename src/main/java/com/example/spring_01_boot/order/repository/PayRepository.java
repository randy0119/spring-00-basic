package com.example.spring_01_boot.order.repository;

import com.example.spring_01_boot.order.repository.entity.Pay;
import com.example.spring_01_boot.order.repository.entity.PayStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PayRepository extends JpaRepository<Pay, Long> {
    Optional<Pay> findByOrderId(Long orderId);
    Optional<Pay> findTopByOrderIdAndPayStatusOrderByCreatedAtDesc(Long orderId, PayStatus payStatus);
}
