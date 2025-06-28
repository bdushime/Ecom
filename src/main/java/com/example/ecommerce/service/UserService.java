package com.example.ecommerce.service;


import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.model.entity.dto.UserRegistrationDto;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.enums.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * Service class handling user-related business logic
 * Implements UserDetailsService for Spring Security integration
 * Manages user registration, authentication, and CRUD operations
 */
@Service
@Transactional
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, CartRepository cartRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Spring Security method to load user by username for authentication
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Register a new user from registration DTO
     */
    public User registerUser(UserRegistrationDto registrationDto) {
        // Validate that username and email don't already exist
        if (userRepository.existsByUsername(registrationDto.getUsername())) {
            throw new RuntimeException("Username already exists");
        }

        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        // Validate password confirmation
        if (!registrationDto.isPasswordMatching()) {
            throw new RuntimeException("Passwords do not match");
        }

        // Create new user entity
        User user = new User();
        user.setUsername(registrationDto.getUsername());
        user.setEmail(registrationDto.getEmail());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setFullName(registrationDto.getFullName());
        user.setRole(registrationDto.getRole());

        // Save user
        User savedUser = userRepository.save(user);

        // Create cart for customer users
        if (savedUser.getRole() == Role.CUSTOMER) {
            Cart cart = new Cart(savedUser);
            cartRepository.save(cart);
        }

        return savedUser;
    }

    /**
     * Find user by ID
     */
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Find user by username
     */
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * Get all customers with their carts (for admin view)
     */
    public List<User> getAllCustomersWithCarts() {
        return userRepository.findAllCustomersWithCarts();
    }

    /**
     * Get all users
     */
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    /**
     * Delete user by ID (admin function)
     */
    public void deleteUser(Long userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            // Cart will be deleted automatically due to cascade settings
            userRepository.deleteById(userId);
        } else {
            throw new RuntimeException("User not found with ID: " + userId);
        }
    }

    /**
     * Check if username exists
     */
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    /**
     * Check if email exists
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}
