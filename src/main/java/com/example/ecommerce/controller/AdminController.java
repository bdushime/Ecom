package com.example.ecommerce.controller;


import com.example.ecommerce.enums.Role;
import com.example.ecommerce.model.entity.dto.ProductDto;
import com.example.ecommerce.model.entity.Product;
import com.example.ecommerce.model.entity.User;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller handling admin operations
 * Manages product CRUD operations and customer management
 * Only accessible to users with ADMIN role
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductService productService;
    private final UserService userService;

    @Autowired
    public AdminController(ProductService productService, UserService userService) {
        this.productService = productService;
        this.userService = userService;
    }

    /**
     * Admin dashboard
     */
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Product> products = productService.getAllProducts();
        List<User> customers = userService.getAllCustomersWithCarts();

        model.addAttribute("productsCount", products.size());
        model.addAttribute("customersCount", customers.size());
        model.addAttribute("recentProducts", products.stream().limit(5).toList());

        return "admin/dashboard";
    }

    // ==================== PRODUCT MANAGEMENT ====================

    /**
     * View all products
     */
    @GetMapping("/products")
    public String products(@RequestParam(value = "search", required = false) String search, Model model) {
        List<Product> products;

        if (search != null && !search.trim().isEmpty()) {
            products = productService.searchProducts(search);
            model.addAttribute("search", search);
        } else {
            products = productService.getProductsOrderedByDate();
        }

        model.addAttribute("products", products);
        return "admin/products";
    }

    /**
     * Add new product form
     */
    @GetMapping("/products/add")
    public String addProductForm(Model model) {
        model.addAttribute("product", new ProductDto());
        return "admin/add-product";
    }

    /**
     * Process add product form
     */
    @PostMapping("/products/add")
    public String addProduct(@Valid @ModelAttribute("product") ProductDto productDto,
                             BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/add-product";
        }

        try {
            productService.createProduct(productDto);
            redirectAttributes.addFlashAttribute("success", "Product added successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to add product: " + e.getMessage());
            return "redirect:/admin/products/add";
        }
    }

    /**
     * Edit product form
     */
    @GetMapping("/products/edit/{id}")
    public String editProductForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> product = productService.findById(id);
        if (product.isPresent()) {
            model.addAttribute("product", productService.convertToDto(product.get()));
            return "admin/edit-product";
        } else {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/admin/products";
        }
    }

    /**
     * Process edit product form
     */
    @PostMapping("/products/edit/{id}")
    public String editProduct(@PathVariable Long id, @Valid @ModelAttribute("product") ProductDto productDto,
                              BindingResult result, RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "admin/edit-product";
        }

        try {
            productService.updateProduct(id, productDto);
            redirectAttributes.addFlashAttribute("success", "Product updated successfully!");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to update product: " + e.getMessage());
            return "redirect:/admin/products/edit/" + id;
        }
    }

    /**
     * Delete product
     */
    @PostMapping("/products/delete/{id}")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            productService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete product: " + e.getMessage());
        }
        return "redirect:/admin/products";
    }

    // ==================== CUSTOMER MANAGEMENT ====================

    /**
     * View all customers with their carts
     */
    @GetMapping("/customers")
    public String customers(Model model) {
        List<User> customers = userService.getAllCustomersWithCarts();
        model.addAttribute("customers", customers);
        return "admin/customers";
    }

    /**
     * Delete customer account
     */
    @PostMapping("/customers/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<User> user = userService.findById(id);
            if (user.isPresent() && user.get().getRole().equals(Role.CUSTOMER)) {
                userService.deleteUser(id);
                redirectAttributes.addFlashAttribute("success", "Customer account deleted successfully!");
            } else {
                redirectAttributes.addFlashAttribute("error", "Customer not found or invalid user type");
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to delete customer: " + e.getMessage());
        }
        return "redirect:/admin/customers";
    }
}