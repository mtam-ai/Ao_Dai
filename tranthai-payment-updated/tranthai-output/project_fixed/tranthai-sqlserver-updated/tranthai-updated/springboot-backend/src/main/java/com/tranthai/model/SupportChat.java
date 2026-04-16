package com.tranthai.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "support_chats")
public class SupportChat {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long customerId;
    private String customerName;

    @Column(columnDefinition = "TEXT")
    private String messages = "[]"; // JSON array of {sender, text, time}

    private String status = "open"; // open | closed

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();
}
