package com.tranthai.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import java.time.LocalDateTime;

public class SupportDtos {

    @Data
    public static class SendMessageRequest {
        @NotBlank
        private String sender;   // "customer" | "staff"

        @NotBlank
        private String text;
    }

    @Data
    public static class SupportChatResponse {
        private Long   id;
        private Long   customerId;
        private String customerName;
        private String messages;   // JSON string
        private String status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
