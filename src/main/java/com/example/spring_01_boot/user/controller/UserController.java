package com.example.spring_01_boot.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import lombok.RequiredArgsConstructor;
import com.example.spring_01_boot.user.service.UserService;
import com.example.spring_01_boot.user.controller.dto.joinRequest;
import org.springframework.web.bind.annotation.RequestBody;
import jakarta.validation.Valid;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/hello")
    public String hello() {
        return "Hello, World!";
    }

    @PostMapping("/join")
    public String join(@Valid @RequestBody joinRequest request)
    {
        String id = request.getId();
        String name = request.getName();
        String email = request.getEmail();
        String password = request.getPassword();

        String result = userService.join(id, name, email, password);


        if ("join success!".equals(result)) {
            return "success!";
        } else {
            return "failed!";
        }
    }
}