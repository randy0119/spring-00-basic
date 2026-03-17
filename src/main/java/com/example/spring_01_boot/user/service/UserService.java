package com.example.spring_01_boot.user.service;

public interface UserService {
    String join(
        String id,
        String name,
        String email,
        String password
    );
}