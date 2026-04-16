package com.tranthai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "cart_items")
public class CartItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private Long productId;
    private String productName;
    private Double price;
    private Integer qty = 1;
    private String size;

    @Column(columnDefinition = "TEXT")
    private String image;

    private String cartKey;
    private LocalDateTime createdAt = LocalDateTime.now();
}
