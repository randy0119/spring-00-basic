package com.example.spring_01_boot.remit.repository.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name="remit_request")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
@Setter
public class RemitRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long remitId;
    @Column(name = "hash_code", unique = true,nullable = false)
    private String remitHashcode;
    @Column(name = "requester_id", nullable = false)
    private String requesterId;
    @Column(name = "receiver_id", nullable = false)
    private String receiverId;
    @Min(1)
    @Max(10000000)
    @Column(name = "amount", nullable = false)
    private Integer amount;
    @Column(name = "status", nullable = false)
    private RemitRequestStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    @Column(name = "expired_at", nullable = false)
    private Instant expiredAt;
    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    public RemitRequest(String remit_hashcode, String requester_id, String receiver_id, Integer amount) {
        this.remitHashcode = remit_hashcode;
        this.requesterId = requester_id;
        this.receiverId = receiver_id;
        this.amount = amount;
        this.status = RemitRequestStatus.PENDING;
        this.createdAt = Instant.now();
        this.expiredAt = this.createdAt.plusSeconds(600);
    }

    public void setStatus(RemitRequestStatus status) {
        this.status = status;
        this.updatedAt = Instant.now();
    }
}
