package com.example.spring_01_boot.remit.repository;

import com.example.spring_01_boot.remit.repository.entity.RemitRequest;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RemitRequestRepository extends JpaRepository<RemitRequest, Long> {
}
