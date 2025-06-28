package com.example.ecommerce.repository;

import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.model.entity.CartItem;
import com.example.ecommerce.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for CartItem entity
 * Manages individual items within shopping carts
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find all cart items for a specific cart
     */
    List<CartItem> findByCart(Cart cart);

    /**
     * Find cart item by cart and product (to check if product already in cart)
     */
    Optional<CartItem> findByCartAndProduct(Cart cart, Product product);

    /**
     * Find all cart items for a specific product (useful for product deletion)
     */
    List<CartItem> findByProduct(Product product);

    /**
     * Delete all cart items for a specific cart
     */
    void deleteByCart(Cart cart);

    /**
     * Delete cart item by cart and product
     */
    void deleteByCartAndProduct(Cart cart, Product product);
}