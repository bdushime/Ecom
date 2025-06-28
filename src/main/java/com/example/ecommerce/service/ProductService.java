package com.example.ecommerce.service;

// ProductService.java - Product business logic

import com.example.ecommerce.model.entity.Product;
import com.example.ecommerce.model.entity.dto.ProductDto;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class handling product-related business logic
 * Manages product CRUD operations, search, and inventory management
 */
@Service
@Transactional
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * Create a new product from DTO
     */
    public Product createProduct(ProductDto productDto) {
        Product product = new Product();
        product.setName(productDto.getName());
        product.setDescription(productDto.getDescription());
        product.setPrice(productDto.getPrice());
        product.setStockQuantity(productDto.getStockQuantity());
        product.setImageUrl(productDto.getImageUrl());

        return productRepository.save(product);
    }

    /**
     * Update existing product from DTO
     */
    public Product updateProduct(Long id, ProductDto productDto) {
        Optional<Product> existingProduct = productRepository.findById(id);
        if (existingProduct.isPresent()) {
            Product product = existingProduct.get();
            product.setName(productDto.getName());
            product.setDescription(productDto.getDescription());
            product.setPrice(productDto.getPrice());
            product.setStockQuantity(productDto.getStockQuantity());
            product.setImageUrl(productDto.getImageUrl());

            return productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }

    /**
     * Get all products
     */
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    /**
     * Get all products in stock
     */
    public List<Product> getAllInStockProducts() {
        return productRepository.findAllInStock();
    }

    /**
     * Get products ordered by creation date (newest first)
     */
    public List<Product> getProductsOrderedByDate() {
        return productRepository.findAllByOrderByCreatedAtDesc();
    }

    /**
     * Find product by ID
     */
    public Optional<Product> findById(Long id) {
        return productRepository.findById(id);
    }

    /**
     * Search products by keyword
     */
    public List<Product> searchProducts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllProducts();
        }
        return productRepository.searchProducts(keyword.trim());
    }

    /**
     * Delete product by ID
     */
    public void deleteProduct(Long id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            productRepository.deleteById(id);
        } else {
            throw new RuntimeException("Product not found with ID: " + id);
        }
    }

    /**
     * Convert Product entity to ProductDto
     */
    public ProductDto convertToDto(Product product) {
        return new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getImageUrl()
        );
    }

    /**
     * Check if product has sufficient stock
     */
    public boolean hasStock(Long productId, Integer quantity) {
        Optional<Product> product = productRepository.findById(productId);
        return product.isPresent() && product.get().getStockQuantity() >= quantity;
    }

    /**
     * Reduce stock quantity (when adding to cart)
     */
    public void reduceStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            if (product.getStockQuantity() >= quantity) {
                product.setStockQuantity(product.getStockQuantity() - quantity);
                productRepository.save(product);
            } else {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }
        } else {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
    }

    /**
     * Increase stock quantity (when removing from cart)
     */
    public void increaseStock(Long productId, Integer quantity) {
        Optional<Product> productOpt = productRepository.findById(productId);
        if (productOpt.isPresent()) {
            Product product = productOpt.get();
            product.setStockQuantity(product.getStockQuantity() + quantity);
            productRepository.save(product);
        } else {
            throw new RuntimeException("Product not found with ID: " + productId);
        }
    }
}
