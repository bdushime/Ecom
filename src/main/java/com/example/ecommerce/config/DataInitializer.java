package com.example.ecommerce.config;

import com.example.ecommerce.model.entity.Product;
import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import com.example.ecommerce.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

/**
 * Data initialization class that runs on application startup
 * Creates sample users and products for demonstration purposes
 * Implements CommandLineRunner to execute after application context loads
 */
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public DataInitializer(UserRepository userRepository,
                           ProductRepository productRepository,
                           CartRepository cartRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.cartRepository = cartRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Only initialize data if database is empty
        if (userRepository.count() == 0) {
            initializeUsers();
        }

        if (productRepository.count() == 0) {
            initializeProducts();
        }
    }

    /**
     * Initialize sample users (admin and customers)
     */
    private void initializeUsers() {
        // Create admin user
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@ecommerce.com");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setFullName("Administrator");
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);

        // Create sample customers
        List<User> customers = Arrays.asList(
                createCustomer("john_doe", "john@example.com", "password123", "John Doe"),
                createCustomer("jane_smith", "jane@example.com", "password123", "Jane Smith"),
                createCustomer("bob_wilson", "bob@example.com", "password123", "Bob Wilson")
        );

        userRepository.saveAll(customers);

        // Create carts for customers
        for (User customer : customers) {
            Cart cart = new Cart(customer);
            cartRepository.save(cart);
        }

        System.out.println("✓ Sample users initialized:");
        System.out.println("  - Admin: username=admin, password=admin123");
        System.out.println("  - Customer: username=john_doe, password=password123");
        System.out.println("  - Customer: username=jane_smith, password=password123");
        System.out.println("  - Customer: username=bob_wilson, password=password123");
    }

    /**
     * Initialize sample products
     */
    private void initializeProducts() {
        List<Product> products = Arrays.asList(
                createProduct("Laptop Computer",
                        "High-performance laptop with 16GB RAM and 512GB SSD. Perfect for work and gaming.",
                        new BigDecimal("999.99"), 25,
                        "https://images.unsplash.com/photo-1496181133206-80ce9b88a853?w=400"),

                createProduct("Wireless Headphones",
                        "Premium noise-cancelling wireless headphones with 30-hour battery life.",
                        new BigDecimal("249.99"), 50,
                        "https://images.unsplash.com/photo-1505740420928-5e560c06d30e?w=400"),

                createProduct("Smart Phone",
                        "Latest smartphone with advanced camera system and lightning-fast processor.",
                        new BigDecimal("799.99"), 30,
                        "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=400"),

                createProduct("Coffee Maker",
                        "Programmable coffee maker with built-in grinder and thermal carafe.",
                        new BigDecimal("129.99"), 15,
                        "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400"),

                createProduct("Bluetooth Speaker",
                        "Portable Bluetooth speaker with 360-degree sound and waterproof design.",
                        new BigDecimal("89.99"), 40,
                        "https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?w=400"),

                createProduct("Running Shoes",
                        "Lightweight running shoes with advanced cushioning and breathable mesh.",
                        new BigDecimal("159.99"), 60,
                        "https://images.unsplash.com/photo-1542291026-7eec264c27ff?w=400"),

                createProduct("Gaming Mouse",
                        "High-precision gaming mouse with customizable RGB lighting and programmable buttons.",
                        new BigDecimal("69.99"), 35,
                        "https://images.unsplash.com/photo-1527864550417-7fd91fc51a46?w=400"),

                createProduct("Backpack",
                        "Durable travel backpack with multiple compartments and laptop sleeve.",
                        new BigDecimal("79.99"), 20,
                        "https://images.unsplash.com/photo-1553062407-98eeb64c6a62?w=400"),

                createProduct("Desk Lamp",
                        "LED desk lamp with adjustable brightness and USB charging port.",
                        new BigDecimal("49.99"), 25,
                        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=400"),

                createProduct("Water Bottle",
                        "Insulated stainless steel water bottle that keeps drinks cold for 24 hours.",
                        new BigDecimal("29.99"), 80,
                        "https://images.unsplash.com/photo-1602143407151-7111542de6e8?w=400")
        );

        productRepository.saveAll(products);
        System.out.println("✓ Sample products initialized: " + products.size() + " products added");
    }

    /**
     * Helper method to create a customer user
     */
    private User createCustomer(String username, String email, String password, String fullName) {
        User customer = new User();
        customer.setUsername(username);
        customer.setEmail(email);
        customer.setPassword(passwordEncoder.encode(password));
        customer.setFullName(fullName);
        customer.setRole(Role.CUSTOMER);
        return customer;
    }

    /**
     * Helper method to create a product
     */
    private Product createProduct(String name, String description, BigDecimal price,
                                  Integer stockQuantity, String imageUrl) {
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.setPrice(price);
        product.setStockQuantity(stockQuantity);
        product.setImageUrl(imageUrl);
        return product;
    }
}