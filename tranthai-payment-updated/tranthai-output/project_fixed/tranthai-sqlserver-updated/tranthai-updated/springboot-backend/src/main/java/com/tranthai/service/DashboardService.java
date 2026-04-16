package com.tranthai.service;

import com.tranthai.repository.CustomerAccountRepository;
import com.tranthai.repository.OrderRepository;
import com.tranthai.repository.ProductRepository;
import com.tranthai.repository.StaffRepository;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;
    private final CustomerAccountRepository customerRepo;
    private final StaffRepository staffRepo;

    public DashboardStats getStats() {
        DashboardStats stats = new DashboardStats();
        stats.setTotalOrders(orderRepo.count());
        stats.setTotalProducts(productRepo.countByDeletedFalse());
        stats.setTotalCustomers(customerRepo.count());
        stats.setTotalStaff(staffRepo.count());

        double revenue = orderRepo.findByStatus("done")
                .stream()
                .mapToDouble(o -> o.getTotal() != null ? o.getTotal() : 0)
                .sum();
        stats.setTotalRevenue(revenue);

        stats.setPendingOrders(orderRepo.findByStatus("pending").size());

        // Đơn chờ xác nhận thanh toán chuyển khoản (≥5 triệu, bank, chưa được xác nhận)
        stats.setAwaitingPaymentOrders(orderRepo.findByStatus("awaiting_payment").size());

        return stats;
    }

    @Data
    public static class DashboardStats {
        private long   totalOrders;
        private long   totalProducts;
        private long   totalCustomers;
        private long   totalStaff;
        private double totalRevenue;
        private int    pendingOrders;
        private int    awaitingPaymentOrders;
    }
}