package com.tranthai.controller;

import com.tranthai.dto.AuthDtos.*;
import com.tranthai.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/admin/login")
    public ResponseEntity<AuthResponse> adminLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.adminLogin(req));
    }

    @PostMapping("/staff/login")
    public ResponseEntity<AuthResponse> staffLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.staffLogin(req));
    }

    @PostMapping("/customer/login")
    public ResponseEntity<AuthResponse> customerLogin(@Valid @RequestBody LoginRequest req) {
        return ResponseEntity.ok(authService.customerLogin(req));
    }

    @PostMapping("/customer/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest req) {
        return ResponseEntity.ok(authService.register(req));
    }

    @PutMapping("/customer/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest req) {
        authService.changePassword(req);
        return ResponseEntity.noContent().build();
    }
}
