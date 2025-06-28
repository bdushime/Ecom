package com.example.ecommerce.controller;


import com.example.ecommerce.model.entity.Cart;
import com.example.ecommerce.model.entity.Product;
import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller handling customer operations
 * Manages product browsing and shopping cart functionality
 * Only accessible to users with CUSTOMER role
 */
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final ProductService productService;
    private final CartService cartService;
    private final UserService userService;

    @Autowired
    public CustomerController(ProductService productService, CartService cartService, UserService userService) {
        this.productService = productService;
        this.cartService = cartService;
        this.userService = userService;
    }

    /**
     * Customer dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            List<Product> availableProducts = productService.getAllInStockProducts();
            Optional<Cart> cart = cartService.getCartByCustomerId(user.get().getId());

            model.addAttribute("customer", user.get());
            model.addAttribute("availableProductsCount", availableProducts.size());
            model.addAttribute("cartItemsCount", cart.map(Cart::getTotalItems).orElse(0));
            model.addAttribute("recentProducts", availableProducts.stream().limit(6).toList());
        }

        return "customer/dashboard";
    }

    /**
     * Browse all available products
     */
    @GetMapping("/products")
    public String products(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Product> products;

        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search).stream()
                    .filter(p -> p.getStockQuantity() > 0)
                    .toList();
            model.addAttribute("search", search);
        } else {
            products = productService.getAllInStockProducts();
        }

        model.addAttribute("products", products);
        return "customer/products";
    }

    /**
     * Add product to cart
     */
    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId,
                            @RequestParam(defaultValue = "1") Integer quantity,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            Optional<User> user = userService.findByUsername(username);

            if (user.isPresent()) {
                cartService.addToCart(user.get(), productId, quantity);
                redirectAttributes.addFlashAttribute("success", "Product added to cart successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "User not found");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product to cart: " + e.getMessage());
        }

        return "redirect:/customer/products";
    }

    /**
     * View shopping cart
     */
    @GetMapping("/cart")
    public String viewCart(Model model, Authentication authentication) {
        String username = authentication.getName();
        Optional<User> user = userService.findByUsername(username);

        if (user.isPresent()) {
            Optional<Cart> cart = cartService.getCartWithItems(user.get().getId());
            model.addAttribute("cart", cart.orElse(null));
        }

        return "customer/cart";
    }

    /**
     * Update cart item quantity
     */
    @PostMapping("/cart/update/{cartItemId}")
    public String updateCartItem(@PathVariable Long cartItemId,
                                 @RequestParam Integer quantity,
                                 RedirectAttributes redirectAttributes) {
        try {
            cartService.updateCartItemQuantity(cartItemId, quantity);
            redirectAttributes.addFlashAttribute("success", "Cart updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update cart: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    /**
     * Remove item from cart
     */
    @PostMapping("/cart/remove/{cartItemId}")
    public String removeFromCart(@PathVariable Long cartItemId, RedirectAttributes redirectAttributes) {
        try {
            cartService.removeFromCart(cartItemId);
            redirectAttributes.addFlashAttribute("success", "Item removed from cart!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to remove item: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }

    /**
     * Clear entire cart
     */
    @PostMapping("/cart/clear")
    public String clearCart(Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            String username = authentication.getName();
            Optional<User> user = userService.findByUsername(username);

            if (user.isPresent()) {
                cartService.clearCart(user.get().getId());
                redirectAttributes.addFlashAttribute("success", "Cart cleared successfully!");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to clear cart: " + e.getMessage());
        }

        return "redirect:/customer/cart";
    }
}