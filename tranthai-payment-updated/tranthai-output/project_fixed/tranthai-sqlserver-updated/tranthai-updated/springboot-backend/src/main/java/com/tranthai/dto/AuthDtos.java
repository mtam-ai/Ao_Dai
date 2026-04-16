package com.tranthai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

// ═══════════════════════════════════════════════════════════
//  AUTH DTOs
// ═══════════════════════════════════════════════════════════

public class AuthDtos {

    @Data
    public static class LoginRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
    }

    @Data
    public static class RegisterRequest {
        @NotBlank
        private String name;
        @NotBlank @Email
        private String email;
        @NotBlank
        private String password;
        private String phone;
        private String address;
    }

    @Data
    public static class ChangePasswordRequest {
        @NotBlank @Email
        private String email;
        @NotBlank
        private String oldPassword;
        @NotBlank
        private String newPassword;
    }

    @Data
    public static class AuthResponse {
        private String token;
        private String role;
        private Long   id;
        private String name;
        private String email;

        public AuthResponse(String token, String role, Long id, String name, String email) {
            this.token = token; this.role = role; this.id = id;
            this.name = name;   this.email = email;
        }
    }
}
