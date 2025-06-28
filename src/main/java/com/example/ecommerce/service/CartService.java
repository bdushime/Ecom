package com.example.ecommerce.service;


import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.model.entity.CartItem;
import com.example.ecommerce.model.entity.Product;
import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.CartItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service class handling shopping cart business logic
 * Manages cart operations like adding, updating, and removing items
 */
@Service
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    @Autowired
    public CartService(CartRepository cartRepository, CartItemRepository cartItemRepository,
                       ProductService productService) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productService = productService;
    }

    /**
     * Get or create cart for customer
     */
    public Cart getOrCreateCart(User customer) {
        Optional<Cart> existingCart = cartRepository.findByCustomer(customer);
        if (existingCart.isPresent()) {
            return existingCart.get();
        } else {
            Cart newCart = new Cart(customer);
            return cartRepository.save(newCart);
        }
    }

    /**
     * Get customer's cart with items
     */
    public Optional<Cart> getCartWithItems(Long customerId) {
        return cartRepository.findByCustomerIdWithItems(customerId);
    }

    /**
     * Add product to cart
     */
    public void addToCart(User customer, Long productId, Integer quantity) {
        // Validate product exists and has stock
        Optional<Product> productOpt = productService.findById(productId);
        if (!productOpt.isPresent()) {
            throw new RuntimeException("Product not found");
        }

        Product product = productOpt.get();
        if (!productService.hasStock(productId, quantity)) {
            throw new RuntimeException("Insufficient stock for product: " + product.getName());
        }

        // Get or create cart
        Cart cart = getOrCreateCart(customer);

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartAndProduct(cart, product);

        if (existingItem.isPresent()) {
            // Update quantity if product already in cart
            CartItem cartItem = existingItem.get();
            Integer newQuantity = cartItem.getQuantity() + quantity;

            if (productService.hasStock(productId, newQuantity - cartItem.getQuantity())) {
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            } else {
                throw new RuntimeException("Cannot add more items. Insufficient stock.");
            }
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem(cart, product, quantity);
            cartItemRepository.save(cartItem);
        }
    }

    /**
     * Update cart item quantity
     */
    public void updateCartItemQuantity(Long cartItemId, Integer newQuantity) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (!cartItemOpt.isPresent()) {
            throw new RuntimeException("Cart item not found");
        }

        CartItem cartItem = cartItemOpt.get();

        if (newQuantity <= 0) {
            // Remove item if quantity is 0 or negative
            cartItemRepository.delete(cartItem);
        } else {
            // Check stock availability
            if (productService.hasStock(cartItem.getProduct().getId(), newQuantity)) {
                cartItem.setQuantity(newQuantity);
                cartItemRepository.save(cartItem);
            } else {
                throw new RuntimeException("Insufficient stock for the requested quantity");
            }
        }
    }

    /**
     * Remove item from cart
     */
    public void removeFromCart(Long cartItemId) {
        Optional<CartItem> cartItemOpt = cartItemRepository.findById(cartItemId);
        if (cartItemOpt.isPresent()) {
            cartItemRepository.delete(cartItemOpt.get());
        } else {
            throw new RuntimeException("Cart item not found");
        }
    }

    /**
     * Clear entire cart
     */
    public void clearCart(Long customerId) {
        Optional<Cart> cartOpt = cartRepository.findByCustomerId(customerId);
        if (cartOpt.isPresent()) {
            Cart cart = cartOpt.get();
            cartItemRepository.deleteByCart(cart);
        }
    }

    /**
     * Get cart by customer ID
     */
    public Optional<Cart> getCartByCustomerId(Long customerId) {
        return cartRepository.findByCustomerId(customerId);
    }
}