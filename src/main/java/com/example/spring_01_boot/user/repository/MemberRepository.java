package com.example.spring_01_boot.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.spring_01_boot.user.repository.entity.Member;

public interface MemberRepository extends JpaRepository<Member, Long> {
}