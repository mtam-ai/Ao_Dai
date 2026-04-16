package com.tranthai.controller;

import com.tranthai.model.CustomerAccount;
import com.tranthai.repository.CustomerAccountRepository;
import com.tranthai.security.JwtUtils;
import com.tranthai.service.CustomerService;
import com.tranthai.service.CustomerService.CustomerResponse;
import com.tranthai.service.CustomerService.UpdateStatusRequest;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;
    private final CustomerAccountRepository customerRepo;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<List<CustomerResponse>> getAll() {
        return ResponseEntity.ok(customerService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerResponse> updateStatus(@PathVariable Long id,
                                                          @RequestBody UpdateStatusRequest req) {
        return ResponseEntity.ok(customerService.updateStatus(id, req.getStatus()));
    }

    // Update own profile
    @PutMapping("/profile")
    public ResponseEntity<?> updateProfile(@RequestHeader("Authorization") String auth,
                                            @RequestBody ProfileUpdateRequest req) {
        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).build();
        CustomerAccount c = customerRepo.findByEmail(email).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        if (req.getName() != null) c.setName(req.getName());
        if (req.getPhone() != null) c.setPhone(req.getPhone());
        if (req.getAddress() != null) c.setAddress(req.getAddress());
        // Email change - check uniqueness
        if (req.getEmail() != null && !req.getEmail().equals(email)) {
            if (customerRepo.existsByEmail(req.getEmail()))
                return ResponseEntity.badRequest().body(java.util.Map.of("message", "Email đã được sử dụng"));
            c.setEmail(req.getEmail());
        }
        customerRepo.save(c);
        return ResponseEntity.ok(java.util.Map.of(
            "id", c.getId(), "name", c.getName(), "email", c.getEmail(),
            "phone", c.getPhone() != null ? c.getPhone() : "",
            "address", c.getAddress() != null ? c.getAddress() : ""
        ));
    }

    // Change own password
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestHeader("Authorization") String auth,
                                             @RequestBody ChangePasswordRequest req) {
        String email = extractEmail(auth);
        if (email == null) return ResponseEntity.status(401).build();
        CustomerAccount c = customerRepo.findByEmail(email).orElse(null);
        if (c == null) return ResponseEntity.notFound().build();
        if (!passwordEncoder.matches(req.getOldPassword(), c.getPassword()))
            return ResponseEntity.badRequest().body(java.util.Map.of("message", "Mật khẩu hiện tại không đúng"));
        c.setPassword(passwordEncoder.encode(req.getNewPassword()));
        customerRepo.save(c);
        return ResponseEntity.ok(java.util.Map.of("message", "Đổi mật khẩu thành công"));
    }

    private String extractEmail(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try { return jwtUtils.getSubject(authHeader.substring(7)); }
        catch (Exception e) { return null; }
    }

    @Data static class ProfileUpdateRequest {
        private String name, email, phone, address;
    }
    @Data static class ChangePasswordRequest {
        private String oldPassword, newPassword;
    }
}
