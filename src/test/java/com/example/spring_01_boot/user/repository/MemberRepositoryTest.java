package com.example.spring_01_boot.user.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.spring_01_boot.user.repository.entity.Member;
import org.junit.jupiter.api.Test;

@SpringBootTest
public class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void crudTest() {
        Member member = Member.builder()
            .id("test")
            .name("test")
            .email("test@test.com")
            .password("test")
            .build();

        // create test
        memberRepository.save(member);

        // get test
        Member foundMember = memberRepository.findById(1L).get();
    }
}