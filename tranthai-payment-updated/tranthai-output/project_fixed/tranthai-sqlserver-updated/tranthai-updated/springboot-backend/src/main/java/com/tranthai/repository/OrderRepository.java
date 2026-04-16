package com.tranthai.repository;

import com.tranthai.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCustomerId(Long customerId);
    List<Order> findByStatus(String status);
    List<Order> findAllByOrderByOrderDateDesc();
    // Tìm đơn theo mã nội dung chuyển khoản (dùng cho SePay webhook)
    Optional<Order> findByTransferCode(String transferCode);
}