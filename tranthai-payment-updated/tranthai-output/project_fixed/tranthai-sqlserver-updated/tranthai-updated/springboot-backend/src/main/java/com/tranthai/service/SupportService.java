package com.tranthai.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranthai.dto.SupportDtos.*;
import com.tranthai.model.SupportChat;
import com.tranthai.repository.SupportChatRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportService {

    private final SupportChatRepository repo;
    private final ObjectMapper objectMapper;

    public List<SupportChatResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public SupportChatResponse openChat(Long customerId, String customerName) {
        SupportChat chat = new SupportChat();
        chat.setCustomerId(customerId);
        chat.setCustomerName(customerName);
        return toResponse(repo.save(chat));
    }

    public SupportChatResponse sendMessage(Long chatId, SendMessageRequest req) {
        SupportChat chat = findOrThrow(chatId);
        try {
            List<Map<String, Object>> messages = objectMapper.readValue(
                    chat.getMessages(), new TypeReference<>() {});
            messages.add(Map.of(
                    "sender", req.getSender(),
                    "text",   req.getText(),
                    "time",   LocalDateTime.now().toString()
            ));
            chat.setMessages(objectMapper.writeValueAsString(messages));
        } catch (Exception e) {
            chat.setMessages("[]");
        }
        chat.setUpdatedAt(LocalDateTime.now());
        return toResponse(repo.save(chat));
    }

    public SupportChatResponse closeChat(Long chatId) {
        SupportChat chat = findOrThrow(chatId);
        chat.setStatus("closed");
        chat.setUpdatedAt(LocalDateTime.now());
        return toResponse(repo.save(chat));
    }

    // ── helpers ──────────────────────────────────────────────
    private SupportChat findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy chat id=" + id));
    }

    private SupportChatResponse toResponse(SupportChat c) {
        SupportChatResponse r = new SupportChatResponse();
        r.setId(c.getId());
        r.setCustomerId(c.getCustomerId());
        r.setCustomerName(c.getCustomerName());
        r.setMessages(c.getMessages());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt());
        r.setUpdatedAt(c.getUpdatedAt());
        return r;
    }
}
