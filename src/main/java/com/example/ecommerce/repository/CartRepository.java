package com.example.ecommerce.repository;

import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository interface for Cart entity
 * Handles shopping cart data operations
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by customer
     */
    Optional<Cart> findByCustomer(User customer);

    /**
     * Find cart by customer ID
     */
    Optional<Cart> findByCustomerId(Long customerId);

    /**
     * Find cart with cart items for a customer
     */
    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.cartItems ci LEFT JOIN FETCH ci.product WHERE c.customer.id = :customerId")
    Optional<Cart> findByCustomerIdWithItems(Long customerId);

    /**
     * Check if customer has a cart
     */
    boolean existsByCustomerId(Long customerId);
}
