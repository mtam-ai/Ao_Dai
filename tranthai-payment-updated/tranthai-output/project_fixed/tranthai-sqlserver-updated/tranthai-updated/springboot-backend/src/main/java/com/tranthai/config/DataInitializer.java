package com.tranthai.config;

import com.tranthai.model.Staff;
import com.tranthai.repository.StaffRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final StaffRepository staffRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!staffRepo.existsByEmail("admin@tranthai.com")) {
            Staff admin = new Staff();
            admin.setName("Admin");
            admin.setEmail("admin@tranthai.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            admin.setStatus("ACTIVE");
            staffRepo.save(admin);
            log.info("✅ Tạo tài khoản admin mặc định: admin@tranthai.com / admin123");
        }
    }
}
