package com.tranthai.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

public class StaffDtos {

    @Data
    public static class StaffRequest {
        @NotBlank
        private String name;

        @NotBlank @Email
        private String email;

        private String password;   // optional on update

        private String phone;

        private String role;

        private String status;
    }

    @Data
    public static class StaffResponse {
        private Long   id;
        private String name;
        private String email;
        private String phone;
        private String role;
        private String status;
        private String createdAt;
    }
}
