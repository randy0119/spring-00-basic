package com.example.spring_01_boot.user.repository;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.spring_01_boot.user.repository.entity.Member;

import java.util.NoSuchElementException;

import org.junit.jupiter.api.Test;

@SpringBootTest
public class MemberRepositoryTest {
    @Autowired
    private MemberRepository memberRepository;

    @Test
    public void crudTest() {
        Member member = Member.builder()
            .id("test-id")
            .name("test-name")
            .email("test-email@test.com")
            .password("test-password")
            .build();

        // create test
        memberRepository.save(member);

        // get test
        Member foundMember = memberRepository.findById(1L).get();
        System.out.println(foundMember);

        // delete test
        memberRepository.delete(member);

        // get test
        try {
            Member foundMember2 = memberRepository.findById(1L).get();
            System.out.println(foundMember2);
        } catch (NoSuchElementException e) {
            System.out.println("Member not found: " + e.getMessage());
            System.out.println("delete test success!");
        }
    }
}