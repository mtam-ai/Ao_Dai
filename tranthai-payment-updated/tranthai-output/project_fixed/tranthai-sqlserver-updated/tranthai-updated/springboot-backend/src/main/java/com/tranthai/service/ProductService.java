package com.tranthai.service;

import com.tranthai.dto.ProductDtos.*;
import com.tranthai.model.Product;
import com.tranthai.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository repo;

    // Public: chỉ sản phẩm đang bán, chưa xóa
    public List<ProductResponse> getPublic() {
        return repo.findByDeletedFalseAndStatus("Đang bán")
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    // Admin/Staff: tất cả sản phẩm kể cả đã xóa mềm
    public List<ProductResponse> getAll() {
        return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public ProductResponse getById(Long id) {
        return toResponse(findOrThrow(id));
    }

    public ProductResponse create(ProductRequest req) {
        Product p = new Product();
        applyRequest(p, req);
        return toResponse(repo.save(p));
    }

    public ProductResponse update(Long id, ProductRequest req) {
        Product p = findOrThrow(id);
        applyRequest(p, req);
        return toResponse(repo.save(p));
    }

    public void softDelete(Long id) {
        Product p = findOrThrow(id);
        p.setDeleted(true);
        p.setDeletedAt(LocalDateTime.now());
        repo.save(p);
    }

    public ProductResponse restore(Long id) {
        Product p = findOrThrow(id);
        p.setDeleted(false);
        p.setDeletedAt(null);
        return toResponse(repo.save(p));
    }

    // ── helpers ──────────────────────────────────────────────
    private Product findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm id=" + id));
    }

    private void applyRequest(Product p, ProductRequest req) {
        p.setName(req.getName());
        p.setPrice(req.getPrice());
        p.setDescription(req.getDescription());
        p.setStock(req.getStock() != null ? req.getStock() : 0);
        p.setStatus(req.getStatus() != null ? req.getStatus() : "Đang bán");
        p.setImage(req.getImage());
        p.setCategory(req.getCategory());
    }

    private ProductResponse toResponse(Product p) {
        ProductResponse r = new ProductResponse();
        r.setId(p.getId());
        r.setName(p.getName());
        r.setPrice(p.getPrice());
        r.setDescription(p.getDescription());
        r.setStock(p.getStock());
        r.setStatus(p.getStatus());
        r.setImage(p.getImage());
        r.setCategory(p.getCategory());
        r.setDeleted(p.getDeleted());
        r.setDeletedAt(p.getDeletedAt() != null ? p.getDeletedAt().toString() : null);
        return r;
    }
}
