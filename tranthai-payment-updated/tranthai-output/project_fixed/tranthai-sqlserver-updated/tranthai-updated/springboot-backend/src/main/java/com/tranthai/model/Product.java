package com.tranthai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "products")
public class Product {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private Double price;

    @Column(columnDefinition = "TEXT")
    private String description;

    private Integer stock = 0;
    private String status = "Đang bán";

    @Column(columnDefinition = "TEXT")
    private String image;

    private String category;
    private Boolean deleted = false;
    private LocalDateTime deletedAt;
    private LocalDateTime createdAt = LocalDateTime.now();
}
