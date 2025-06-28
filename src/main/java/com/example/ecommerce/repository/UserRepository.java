package com.example.ecommerce.repository;

import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for User entity
 * Extends JpaRepository to provide basic CRUD operations
 * Contains custom query methods for user-specific operations
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by username (used for authentication)
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email (used for registration validation)
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username already exists (for registration validation)
     */
    boolean existsByUsername(String username);

    /**
     * Check if email already exists (for registration validation)
     */
    boolean existsByEmail(String email);

    /**
     * Find all users with a specific role
     */
    List<User> findByRole(Role role);

    /**
     * Find all customers (users with CUSTOMER role)
     * Custom query to get customers with their cart information
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.cart WHERE u.role = 'CUSTOMER'")
    List<User> findAllCustomersWithCarts();
}
