package com.tranthai.service;

import com.tranthai.dto.AuthDtos.*;
import com.tranthai.model.CustomerAccount;
import com.tranthai.model.Staff;
import com.tranthai.repository.CustomerAccountRepository;
import com.tranthai.repository.StaffRepository;
import com.tranthai.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final StaffRepository staffRepo;
    private final CustomerAccountRepository customerRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    // ── Admin login ──────────────────────────────────────────
    public AuthResponse adminLogin(LoginRequest req) {
        Staff staff = staffRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (!"ADMIN".equals(staff.getRole()))
            throw new RuntimeException("Tài khoản không có quyền admin");
        if (!"ACTIVE".equals(staff.getStatus()))
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
        if (!passwordEncoder.matches(req.getPassword(), staff.getPassword()))
            throw new RuntimeException("Mật khẩu không đúng");
        String token = jwtUtils.generateToken(staff.getEmail(), "ADMIN");
        return new AuthResponse(token, "ADMIN", staff.getId(), staff.getName(), staff.getEmail());
    }

    // ── Staff login ──────────────────────────────────────────
    public AuthResponse staffLogin(LoginRequest req) {
        Staff staff = staffRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (!"ACTIVE".equals(staff.getStatus()))
            throw new RuntimeException("Tài khoản đã bị vô hiệu hóa");
        if (!passwordEncoder.matches(req.getPassword(), staff.getPassword()))
            throw new RuntimeException("Mật khẩu không đúng");
        String token = jwtUtils.generateToken(staff.getEmail(), staff.getRole());
        return new AuthResponse(token, staff.getRole(), staff.getId(), staff.getName(), staff.getEmail());
    }

    // ── Customer login ───────────────────────────────────────
    public AuthResponse customerLogin(LoginRequest req) {
        CustomerAccount customer = customerRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (!"ACTIVE".equals(customer.getStatus()))
            throw new RuntimeException("Tài khoản đã bị khóa");
        if (!passwordEncoder.matches(req.getPassword(), customer.getPassword()))
            throw new RuntimeException("Mật khẩu không đúng");
        String token = jwtUtils.generateToken(customer.getEmail(), "CUSTOMER");
        return new AuthResponse(token, "CUSTOMER", customer.getId(), customer.getName(), customer.getEmail());
    }

    // ── Customer register ────────────────────────────────────
    public AuthResponse register(RegisterRequest req) {
        if (customerRepo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã được sử dụng");
        CustomerAccount c = new CustomerAccount();
        c.setName(req.getName());
        c.setEmail(req.getEmail());
        c.setPassword(passwordEncoder.encode(req.getPassword()));
        c.setPhone(req.getPhone());
        c.setAddress(req.getAddress());
        customerRepo.save(c);
        String token = jwtUtils.generateToken(c.getEmail(), "CUSTOMER");
        return new AuthResponse(token, "CUSTOMER", c.getId(), c.getName(), c.getEmail());
    }

    // ── Change password ──────────────────────────────────────
    public void changePassword(ChangePasswordRequest req) {
        CustomerAccount c = customerRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Email không tồn tại"));
        if (!passwordEncoder.matches(req.getOldPassword(), c.getPassword()))
            throw new RuntimeException("Mật khẩu cũ không đúng");
        c.setPassword(passwordEncoder.encode(req.getNewPassword()));
        customerRepo.save(c);
    }
}
