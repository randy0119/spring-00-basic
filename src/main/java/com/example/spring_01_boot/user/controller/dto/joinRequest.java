package com.example.spring_01_boot.user.controller.dto;

import lombok.Data;

@Data
public class joinRequest {
    private String id;
    private String name;
    private String email;
    private String password;
}
