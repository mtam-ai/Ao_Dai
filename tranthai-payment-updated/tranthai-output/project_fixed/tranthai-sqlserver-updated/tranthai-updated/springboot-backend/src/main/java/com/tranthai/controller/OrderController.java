package com.tranthai.controller;

import com.tranthai.dto.OrderDtos.*;
import com.tranthai.repository.CustomerAccountRepository;
import com.tranthai.security.JwtUtils;
import com.tranthai.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final JwtUtils jwtUtils;
    private final CustomerAccountRepository customerRepo;

    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAll() {
        return ResponseEntity.ok(orderService.getAll());
    }

    @GetMapping("/my")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@RequestHeader("Authorization") String auth) {
        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).build();
        Long cid = customerRepo.findByEmail(email).map(c -> c.getId()).orElse(null);
        if (cid == null) return ResponseEntity.status(404).build();
        return ResponseEntity.ok(orderService.getByCustomer(cid));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.getById(id));
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<OrderResponse>> getByCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(orderService.getByCustomer(customerId));
    }

    @PostMapping
    public ResponseEntity<OrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        return ResponseEntity.ok(orderService.create(req));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
                                                       @Valid @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(orderService.updateStatus(id, req));
    }

    @PutMapping("/{id}/confirm-payment")
    public ResponseEntity<OrderResponse> confirmPayment(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.confirmPayment(id));
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try { return jwtUtils.getSubject(authHeader.substring(7)); }
        catch (Exception e) { return null; }
    }
}
