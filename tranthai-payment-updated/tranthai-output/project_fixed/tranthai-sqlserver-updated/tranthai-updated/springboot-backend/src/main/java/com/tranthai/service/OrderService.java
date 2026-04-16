package com.tranthai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tranthai.dto.OrderDtos.*;
import com.tranthai.model.Order;
import com.tranthai.repository.OrderRepository;
import com.tranthai.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private static final double HIGH_VALUE_THRESHOLD = 5_000_000.0;

    private final OrderRepository repo;
    private final ObjectMapper objectMapper;
    private final ProductRepository productRepository;

    public List<OrderResponse> getAll() {
        return repo.findAllByOrderByOrderDateDesc()
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    public OrderResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public List<OrderResponse> getByCustomer(Long customerId) {
        return repo.findByCustomerId(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    /**
     * Danh sách đơn đang chờ admin xác nhận đã nhận tiền chuyển khoản.
     * Bao gồm tất cả đơn có status = "awaiting_payment".
     */
    public List<OrderResponse> getAwaitingPayment() {
        return repo.findByStatus("awaiting_payment")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse create(CreateOrderRequest req) {
        // Trừ tồn kho
        if (req.getItems() != null) {
            for (OrderItemRequest item : req.getItems()) {
                if (item.getId() != null && item.getQty() != null && item.getQty() > 0) {
                    productRepository.findById(item.getId()).ifPresent(product -> {
                        int newStock = product.getStock() - item.getQty();
                        if (newStock < 0) newStock = 0;
                        product.setStock(newStock);
                        productRepository.save(product);
                    });
                }
            }
        }

        Order o = new Order();
        o.setCustomerId(req.getCustomerId());
        o.setCustomerName(req.getCustomerName());
        o.setPhone(req.getPhone());
        o.setAddress(req.getAddress());
        o.setTotal(req.getTotal());
        o.setPayment(req.getPayment());
        o.setPaymentStatus("unpaid");
        o.setNote(req.getNote());
        try {
            o.setItems(objectMapper.writeValueAsString(req.getItems()));
        } catch (Exception e) {
            o.setItems("[]");
        }

        boolean isBankPayment = "bank".equalsIgnoreCase(req.getPayment());
        boolean isHighValue    = req.getTotal() != null && req.getTotal() >= HIGH_VALUE_THRESHOLD;

        if (isBankPayment) {
            // Đơn chuyển khoản: ẩn với nhân viên cho đến khi thanh toán xong
            o.setStatus("awaiting_payment");
            Order saved = repo.save(o);
            String code = "AODAI" + saved.getId();
            saved.setTransferCode(code);
            return toResponse(repo.save(saved));
        }

        // COD hoặc bank giá trị thấp: hiện ngay cho nhân viên
        o.setStatus("pending");
        return toResponse(repo.save(o));
    }

    public OrderResponse updateStatus(Long id, UpdateStatusRequest req) {
        Order o = findOrThrow(id);
        o.setStatus(req.getStatus());
        return toResponse(repo.save(o));
    }

    /**
     * Admin xác nhận thủ công đã nhận tiền chuyển khoản.
     * Sau khi xác nhận: paymentStatus → paid, status → pending
     * (đơn hiện thị cho nhân viên và tiếp tục quy trình giao hàng).
     */
    @Transactional
    public OrderResponse confirmPayment(Long id) {
        Order o = findOrThrow(id);
        o.setPaymentStatus("paid");
        if ("awaiting_payment".equals(o.getStatus()) || "pending".equals(o.getStatus())) {
            o.setStatus("pending");
        }
        log.info("Admin xác nhận thanh toán thủ công cho đơn #{}", id);
        return toResponse(repo.save(o));
    }

    /**
     * Xử lý webhook từ SePay.
     * SePay gọi POST /api/webhook/sepay mỗi khi có giao dịch vào tài khoản.
     * Khi khớp: paymentStatus → paid, status → pending (nhân viên bắt đầu thấy đơn).
     * Trả về true nếu khớp được đơn hàng và xác nhận thành công.
     */
    @Transactional
    public boolean handleSePayWebhook(SePayWebhookRequest payload) {
        // Chỉ xử lý giao dịch tiền VÀO (transferType = "in")
        if (!"in".equalsIgnoreCase(payload.getTransferType())) {
            log.info("SePay webhook: bỏ qua giao dịch ra - {}", payload.getId());
            return false;
        }

        // Tìm mã AODAI{id} trong nội dung chuyển khoản
        String content = payload.getContent() != null ? payload.getContent().toUpperCase() : "";
        String code    = payload.getCode()    != null ? payload.getCode().toUpperCase()    : "";
        String matchCode = extractTransferCode(content);
        if (matchCode == null) matchCode = extractTransferCode(code);

        if (matchCode == null) {
            log.warn("SePay webhook: không tìm được mã AODAI trong nội dung '{}'", content);
            return false;
        }

        Optional<Order> opt = repo.findByTransferCode(matchCode);
        if (opt.isEmpty()) {
            log.warn("SePay webhook: không tìm thấy đơn với transferCode={}", matchCode);
            return false;
        }

        Order o = opt.get();

        // Kiểm tra số tiền khớp (cho phép sai lệch ±1000đ do phí)
        if (payload.getTransferAmount() != null &&
                Math.abs(payload.getTransferAmount() - o.getTotal()) > 1000) {
            log.warn("SePay webhook: số tiền không khớp - nhận {} - cần {}", payload.getTransferAmount(), o.getTotal());
            return false;
        }

        // Đã thanh toán rồi thì bỏ qua (idempotent)
        if ("paid".equals(o.getPaymentStatus())) {
            log.info("SePay webhook: đơn {} đã được xác nhận trước đó", o.getId());
            return true;
        }

        // Thanh toán thành công → chuyển sang pending để nhân viên thấy và xử lý
        o.setPaymentStatus("paid");
        o.setStatus("pending");
        repo.save(o);
        log.info("SePay webhook: xác nhận thanh toán thành công cho đơn #{}, chuyển sang pending", o.getId());
        return true;
    }

    /** Tìm pattern AODAI\d+ trong chuỗi nội dung */
    private String extractTransferCode(String text) {
        if (text == null) return null;
        java.util.regex.Matcher m = java.util.regex.Pattern
                .compile("AODAI\\d+").matcher(text.toUpperCase());
        return m.find() ? m.group() : null;
    }

    // ── helpers ──────────────────────────────────────────────
    private Order findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng id=" + id));
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse r = new OrderResponse();
        r.setId(o.getId());
        r.setCustomerId(o.getCustomerId());
        r.setCustomerName(o.getCustomerName());
        r.setPhone(o.getPhone());
        r.setAddress(o.getAddress());
        r.setItems(o.getItems());
        r.setTotal(o.getTotal());
        r.setPayment(o.getPayment());
        r.setPaymentStatus(o.getPaymentStatus());
        r.setTransferCode(o.getTransferCode());
        r.setOrderDate(o.getOrderDate());
        r.setStatus(o.getStatus());
        r.setNote(o.getNote());
        return r;
    }
}