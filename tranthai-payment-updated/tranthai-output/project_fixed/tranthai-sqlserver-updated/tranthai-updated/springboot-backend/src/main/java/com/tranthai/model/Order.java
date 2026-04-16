package com.tranthai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private String customerName;
    private String phone;
    private String address;

    @Column(columnDefinition = "TEXT")
    private String items;   // JSON string

    private Double total;
    private String payment; // cod | bank
    private String paymentStatus = "unpaid"; // unpaid | paid

    /**
     * Trạng thái đơn hàng:
     *   awaiting_payment → đơn bank chờ khách chuyển tiền (ẨN với nhân viên)
     *   pending          → chờ nhân viên xác nhận (đã trả tiền hoặc COD)
     *   confirmed        → nhân viên đã xác nhận
     *   shipping         → đang giao
     *   done             → hoàn thành
     *   cancelled        → đã hủy
     */
    private String status = "pending";

    private String note;

    // Mã nội dung chuyển khoản dùng để đối soát SePay webhook
    // Ví dụ: "AODAI123456"
    private String transferCode;

    private LocalDateTime orderDate = LocalDateTime.now();
}