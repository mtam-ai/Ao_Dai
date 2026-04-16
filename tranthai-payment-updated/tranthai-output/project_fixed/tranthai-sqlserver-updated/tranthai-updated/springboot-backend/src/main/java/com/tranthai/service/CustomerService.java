package com.tranthai.service;

import com.tranthai.model.CustomerAccount;
import com.tranthai.repository.CustomerAccountRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerAccountRepository repo;

    public List<CustomerResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CustomerResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public CustomerResponse updateStatus(Long id, String status) {
        CustomerAccount c = findOrThrow(id);
        c.setStatus(status);
        return toResponse(repo.save(c));
    }

    // ── helpers ──────────────────────────────────────────────
    private CustomerAccount findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng id=" + id));
    }

    private CustomerResponse toResponse(CustomerAccount c) {
        CustomerResponse r = new CustomerResponse();
        r.setId(c.getId());
        r.setName(c.getName());
        r.setEmail(c.getEmail());
        r.setPhone(c.getPhone());
        r.setAddress(c.getAddress());
        r.setStatus(c.getStatus());
        r.setCreatedAt(c.getCreatedAt() != null ? c.getCreatedAt().toString() : null);
        return r;
    }

    // ── inline DTO ───────────────────────────────────────────
    @Data
    public static class CustomerResponse {
        private Long   id;
        private String name;
        private String email;
        private String phone;
        private String address;
        private String status;
        private String createdAt;
    }

    @Data
    public static class UpdateStatusRequest {
        private String status;
    }
}
