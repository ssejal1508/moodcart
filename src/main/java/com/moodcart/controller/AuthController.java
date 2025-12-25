package com.moodcart.controller;

import com.moodcart.dto.AuthRequestDto;
import com.moodcart.dto.AuthResponseDto;
import com.moodcart.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "User authentication endpoints")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/signup")
    @Operation(summary = "Register new user")
    public ResponseEntity<AuthResponseDto> signup(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.signup(request));
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponseDto> login(@Valid @RequestBody AuthRequestDto request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
