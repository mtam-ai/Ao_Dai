package com.tranthai.controller;

import com.tranthai.dto.OrderDtos.SePayWebhookRequest;
import com.tranthai.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Nhận webhook từ SePay khi có giao dịch ngân hàng vào tài khoản.
 *
 * Cách cấu hình trên SePay:
 *   Dashboard → Tài khoản ngân hàng → Webhook URL:
 *   https://<your-domain>/api/webhook/sepay
 *   Secret token: cấu hình trong application.properties (sepay.webhook.secret)
 */
@Slf4j
@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class SePayWebhookController {

    private final OrderService orderService;

    @Value("${sepay.webhook.secret:}")
    private String webhookSecret;

    @PostMapping("/sepay")
    public ResponseEntity<Map<String, Object>> handleSePay(
            @RequestBody SePayWebhookRequest payload,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        // Xác thực token nếu đã cấu hình
        if (!webhookSecret.isEmpty()) {
            String expected = "Apikey " + webhookSecret;
            if (!expected.equals(authHeader)) {
                log.warn("SePay webhook: sai secret token");
                return ResponseEntity.status(401)
                        .body(Map.of("success", false, "message", "Unauthorized"));
            }
        }

        log.info("SePay webhook nhận: id={} amount={} content={}",
                payload.getId(), payload.getTransferAmount(), payload.getContent());

        boolean success = orderService.handleSePayWebhook(payload);

        // SePay yêu cầu trả về {"success": true} để không retry
        return ResponseEntity.ok(Map.of(
                "success", true,
                "matched", success,
                "message", success ? "Đơn hàng đã được xác nhận" : "Không khớp đơn hàng nào"
        ));
    }
}