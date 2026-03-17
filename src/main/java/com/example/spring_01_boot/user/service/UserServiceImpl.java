package com.example.spring_01_boot.user.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.user.repository.MemberRepository;
import com.example.spring_01_boot.user.repository.entity.Member;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final MemberRepository memberRepository;

    @Override
    public String join(String id, String name, String email, String password) {
        String hashedPassword = hashPassword(password);

        Member member = Member.builder()
            .id(id)
            .name(name)
            .email(email)
            .password(hashedPassword)
            .build();
        memberRepository.save(member);
        return "join success!";
    }

    private String hashPassword(String rawPassword) {
        if (rawPassword == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Unable to hash password", e);
        }
    }
}
