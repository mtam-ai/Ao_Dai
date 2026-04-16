package com.tranthai.controller;

import com.tranthai.dto.SupportDtos.*;
import com.tranthai.service.SupportService;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/support")
@RequiredArgsConstructor
public class SupportController {

    private final SupportService supportService;

    @GetMapping
    public ResponseEntity<List<SupportChatResponse>> getAll() {
        return ResponseEntity.ok(supportService.getAll());
    }

    @PostMapping("/chat")
    public ResponseEntity<SupportChatResponse> openChat(@RequestBody OpenChatRequest req) {
        return ResponseEntity.ok(supportService.openChat(req.getCustomerId(), req.getCustomerName()));
    }

    @PostMapping("/{chatId}/messages")
    public ResponseEntity<SupportChatResponse> sendMessage(@PathVariable Long chatId,
                                                            @Valid @RequestBody SendMessageRequest req) {
        return ResponseEntity.ok(supportService.sendMessage(chatId, req));
    }

    @PutMapping("/{chatId}/close")
    public ResponseEntity<SupportChatResponse> closeChat(@PathVariable Long chatId) {
        return ResponseEntity.ok(supportService.closeChat(chatId));
    }

    @Data
    static class OpenChatRequest {
        private Long   customerId;
        private String customerName;
    }
}
