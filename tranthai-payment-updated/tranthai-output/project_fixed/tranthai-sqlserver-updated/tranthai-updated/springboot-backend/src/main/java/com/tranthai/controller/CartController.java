package com.tranthai.controller;

import com.tranthai.model.CartItem;
import com.tranthai.repository.CartItemRepository;
import com.tranthai.repository.CustomerAccountRepository;
import com.tranthai.security.JwtUtils;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartItemRepository cartRepo;
    private final CustomerAccountRepository customerRepo;
    private final JwtUtils jwtUtils;

    private Long getCustomerId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        try {
            String token = authHeader.substring(7);
            String email = jwtUtils.getSubject(token);
            return customerRepo.findByEmail(email).map(c -> c.getId()).orElse(null);
        } catch (Exception e) { return null; }
    }

    @GetMapping
    public ResponseEntity<List<CartItem>> getCart(@RequestHeader("Authorization") String auth) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(cartRepo.findByCustomerId(uid));
    }

    @PostMapping
    public ResponseEntity<CartItem> addToCart(@RequestHeader("Authorization") String auth,
                                              @RequestBody CartItem item) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        item.setCustomerId(uid);
        List<CartItem> existing = cartRepo.findByCustomerId(uid);
        CartItem found = existing.stream()
                .filter(c -> c.getProductId().equals(item.getProductId()) &&
                        safeEqual(c.getSize(), item.getSize()))
                .findFirst().orElse(null);
        if (found != null) {
            found.setQty(found.getQty() + item.getQty());
            return ResponseEntity.ok(cartRepo.save(found));
        }
        return ResponseEntity.ok(cartRepo.save(item));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CartItem> updateQty(@RequestHeader("Authorization") String auth,
                                              @PathVariable Long id,
                                              @RequestBody QtyRequest req) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        return cartRepo.findById(id).map(item -> {
            if (!item.getCustomerId().equals(uid)) return ResponseEntity.status(403).<CartItem>build();
            item.setQty(req.getQty());
            return ResponseEntity.ok(cartRepo.save(item));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/item/{cartKey}")
    public ResponseEntity<Void> removeItem(@RequestHeader("Authorization") String auth,
                                           @PathVariable String cartKey) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        // Dùng findAll + deleteAll để tránh lỗi transaction với custom delete query
        List<CartItem> toDelete = cartRepo.findByCustomerId(uid).stream()
                .filter(c -> cartKey.equals(c.getCartKey()))
                .collect(Collectors.toList());
        cartRepo.deleteAll(toDelete);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestHeader("Authorization") String auth) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        List<CartItem> toDelete = cartRepo.findByCustomerId(uid);
        cartRepo.deleteAll(toDelete);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/checkout-clear")
    public ResponseEntity<Void> clearCheckoutItems(@RequestHeader("Authorization") String auth,
                                                   @RequestBody List<String> cartKeys) {
        Long uid = getCustomerId(auth);
        if (uid == null) return ResponseEntity.status(401).build();
        // Dùng findAll + deleteAll — không cần @Transactional, JpaRepository.deleteAll đã có sẵn
        List<CartItem> toDelete = cartRepo.findByCustomerId(uid).stream()
                .filter(c -> cartKeys.contains(c.getCartKey()))
                .collect(Collectors.toList());
        cartRepo.deleteAll(toDelete);
        return ResponseEntity.noContent().build();
    }

    private boolean safeEqual(String a, String b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        return a.equals(b);
    }

    @Data
    static class QtyRequest { private Integer qty; }
}