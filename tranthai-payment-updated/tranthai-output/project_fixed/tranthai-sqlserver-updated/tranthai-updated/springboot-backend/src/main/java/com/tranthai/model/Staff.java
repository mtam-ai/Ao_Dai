package com.tranthai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "staffs")
public class Staff {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String phone;
    private String role = "STAFF";  // STAFF | ADMIN
    private String status = "ACTIVE"; // ACTIVE | INACTIVE

    private LocalDateTime createdAt = LocalDateTime.now();
}
