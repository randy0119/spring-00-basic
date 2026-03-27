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
public class RemitRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long remit_id;
    @Column(name = "hash_code", unique = true,nullable = false)
    private String remit_hashcode;
    @Column(name = "requester_id", nullable = false)
    private String requester_id;
    @Column(name = "receiver_id", nullable = false)
    private String receiver_id;
    @Min(1)
    @Max(10000000)
    @Column(name = "amount", nullable = false)
    private Integer amount;
    @Column(name = "status", nullable = false)
    private RemitRequestStatus status;
    @Column(name = "created_at", nullable = false)
    private Instant created_at;
    @Column(name = "expired_at", nullable = false)
    private Instant expired_at;
    @Column(name = "updated_at", nullable = true)
    private Instant updated_at;

    public RemitRequest(String remit_hashcode, String requester_id, String receiver_id, Integer amount) {
        this.remit_hashcode = remit_hashcode;
        this.requester_id = requester_id;
        this.receiver_id = receiver_id;
        this.amount = amount;
        this.status = RemitRequestStatus.PENDING;
        this.created_at = Instant.now();
        this.expired_at = this.created_at.plusSeconds(600);
    }

    public void setStatus(RemitRequestStatus status) {
        this.status = status;
        this.updated_at = Instant.now();
    }
}
