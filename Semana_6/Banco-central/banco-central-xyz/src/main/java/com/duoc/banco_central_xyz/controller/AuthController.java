package com.duoc.banco_central_xyz.controller;

import com.duoc.banco_central_xyz.dto.LoginRequestDto;
import com.duoc.banco_central_xyz.dto.LoginResponseDto;
import com.duoc.banco_central_xyz.service.AuthService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public LoginResponseDto login(@RequestBody LoginRequestDto request) {
        String token = authService.login(request.username(), request.password());
        return new LoginResponseDto(token);
    }
}