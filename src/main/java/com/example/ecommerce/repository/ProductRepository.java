package com.example.ecommerce.repository;


import com.example.ecommerce.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Product entity
 * Provides methods for product-specific database operations
 * Includes search and filtering capabilities
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by name (case-insensitive search)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products with stock quantity greater than zero
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity > 0")
    List<Product> findAllInStock();

    /**
     * Find products with stock quantity less than a threshold
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold")
    List<Product> findLowStockProducts(@Param("threshold") Integer threshold);

    /**
     * Search products by name or description
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Product> searchProducts(@Param("keyword") String keyword);

    /**
     * Find products ordered by creation date (newest first)
     */
    List<Product> findAllByOrderByCreatedAtDesc();
}