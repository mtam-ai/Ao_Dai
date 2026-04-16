package com.tranthai.service;

import com.tranthai.dto.StaffDtos.*;
import com.tranthai.model.Staff;
import com.tranthai.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StaffService {

    private final StaffRepository repo;
    private final PasswordEncoder passwordEncoder;

    public List<StaffResponse> getAll() {
        return repo.findByStatusNot("DELETED")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public StaffResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public StaffResponse create(StaffRequest req) {
        if (repo.existsByEmail(req.getEmail()))
            throw new RuntimeException("Email đã tồn tại");
        Staff s = new Staff();
        s.setName(req.getName());
        s.setEmail(req.getEmail());
        s.setPassword(passwordEncoder.encode(req.getPassword()));
        s.setPhone(req.getPhone());
        s.setRole(req.getRole() != null ? req.getRole() : "STAFF");
        return toResponse(repo.save(s));
    }

    public StaffResponse update(Long id, StaffRequest req) {
        Staff s = findOrThrow(id);
        s.setName(req.getName());
        s.setPhone(req.getPhone());
        if (req.getPassword() != null && !req.getPassword().isBlank())
            s.setPassword(passwordEncoder.encode(req.getPassword()));
        if (req.getRole() != null) s.setRole(req.getRole());
        if (req.getStatus() != null) s.setStatus(req.getStatus());
        return toResponse(repo.save(s));
    }

    public void delete(Long id) {
        Staff s = findOrThrow(id);
        s.setStatus("INACTIVE");
        repo.save(s);
    }

    // ── helpers ──────────────────────────────────────────────
    private Staff findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy nhân viên id=" + id));
    }

    private StaffResponse toResponse(Staff s) {
        StaffResponse r = new StaffResponse();
        r.setId(s.getId());
        r.setName(s.getName());
        r.setEmail(s.getEmail());
        r.setPhone(s.getPhone());
        r.setRole(s.getRole());
        r.setStatus(s.getStatus());
        r.setCreatedAt(s.getCreatedAt() != null ? s.getCreatedAt().toString() : null);
        return r;
    }
}
