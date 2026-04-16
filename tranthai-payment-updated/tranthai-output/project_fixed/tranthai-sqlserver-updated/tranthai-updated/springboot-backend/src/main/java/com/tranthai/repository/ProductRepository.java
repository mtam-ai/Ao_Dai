package com.tranthai.repository;

import com.tranthai.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByDeletedFalse();
    List<Product> findByDeletedFalseAndStatus(String status);
    long countByDeletedFalse();
}
