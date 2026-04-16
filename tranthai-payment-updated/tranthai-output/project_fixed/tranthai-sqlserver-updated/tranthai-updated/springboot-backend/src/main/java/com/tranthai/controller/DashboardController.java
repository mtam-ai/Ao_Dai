package com.tranthai.controller;

import com.tranthai.dto.OrderDtos.OrderResponse;
import com.tranthai.service.DashboardService;
import com.tranthai.service.DashboardService.DashboardStats;
import com.tranthai.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;
    private final OrderService orderService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStats> getStats() {
        return ResponseEntity.ok(dashboardService.getStats());
    }

    /**
     * Trả về danh sách đơn hàng chờ xác nhận thanh toán chuyển khoản
     * (status = awaiting_payment). Admin dùng để xác nhận thủ công khi
     * khách chưa dùng SePay webhook hoặc cần xác nhận lại.
     */
    @GetMapping("/awaiting-payment")
    public ResponseEntity<List<OrderResponse>> getAwaitingPayment() {
        return ResponseEntity.ok(orderService.getAwaitingPayment());
    }
}