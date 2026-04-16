package com.tranthai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

public class OrderDtos {

    @Data
    public static class OrderItemRequest {
        private Long   id;
        private String name;
        private Integer qty;
        private Double  price;
    }

    @Data
    public static class CreateOrderRequest {
        private Long   customerId;

        @NotBlank
        private String customerName;

        @NotBlank
        private String phone;

        @NotBlank
        private String address;

        @NotNull
        private List<OrderItemRequest> items;

        @NotNull
        private Double total;

        private String payment;       // "cod" | "bank"
        private String paymentStatus; // "unpaid" | "paid"
        private String note;
    }

    @Data
    public static class UpdateStatusRequest {
        @NotBlank
        private String status; // "pending"|"confirmed"|"shipping"|"done"|"cancelled"
    }

    @Data
    public static class OrderResponse {
        private Long   id;
        private Long   customerId;
        private String customerName;
        private String phone;
        private String address;
        private String items;         // raw JSON string
        private Double total;
        private String payment;
        private String paymentStatus;
        private String transferCode;
        private LocalDateTime orderDate;
        private String status;
        private String note;
    }

    // ── SePay webhook payload ─────────────────────────────────
    // Tham khảo: https://docs.sepay.vn/tich-hop-webhook.html
    @Data
    public static class SePayWebhookRequest {
        private String id;
        private String gateway;           // "Techcombank"
        private String transactionDate;
        private String accountNumber;     // "4404122008"
        private String code;              // nội dung chuyển khoản — dùng để khớp transferCode
        private String content;           // toàn bộ nội dung gốc
        private String transferType;      // "in" | "out"
        private Double transferAmount;
        private Double accumulated;
        private String referenceCode;
        private String description;
    }
}