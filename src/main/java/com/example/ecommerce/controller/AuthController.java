package com.example.ecommerce.controller;

import com.example.ecommerce.model.entity.dto.UserRegistrationDto;
import com.example.ecommerce.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.validation.Valid;

/**
 * Controller handling authentication and user registration
 * Manages login, registration, and dashboard routing based on user role
 */
@Controller
public class AuthController {

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Home page - redirects based on authentication status
     */
    @GetMapping("/")

    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    /**
     * Login page
     */
    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model,
                        Authentication authentication) {

        // If user is already authenticated, redirect to dashboard
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }

        if (error != null) {
            model.addAttribute("error", "Invalid username or password");
        }
        if (logout != null) {
            model.addAttribute("message", "You have been logged out successfully");
        }
        return "auth/login";
    }

    /**
     * Registration page
     */
    @GetMapping("/register")
    public String register(Model model, Authentication authentication) {
        // If user is already authenticated, redirect to dashboard
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getName().equals("anonymousUser")) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", new UserRegistrationDto());
        return "auth/register";
    }

    /**
     * Process registration form
     */
    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                               BindingResult result, Model model, RedirectAttributes redirectAttributes) {

        // Check for validation errors
        if (result.hasErrors()) {
            return "auth/register";
        }

        // Additional validation for password matching
        if (!registrationDto.isPasswordMatching()) {
            model.addAttribute("passwordError", "Passwords do not match");
            return "auth/register";
        }

        try {
            // Check if username or email already exists
            if (userService.existsByUsername(registrationDto.getUsername())) {
                model.addAttribute("usernameError", "Username already exists");
                return "auth/register";
            }

            if (userService.existsByEmail(registrationDto.getEmail())) {
                model.addAttribute("emailError", "Email already exists");
                return "auth/register";
            }

            // Register the user
            userService.registerUser(registrationDto);
            redirectAttributes.addFlashAttribute("message", "Registration successful! Please log in.");
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "auth/register";
        }
    }

    /**
     * Dashboard routing based on user role
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getName().equals("anonymousUser")) {
            return "redirect:/login";
        }

        if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_CUSTOMER"))) {
            return "redirect:/customer/dashboard";
        }

        // Fallback - should not happen with proper role assignment
        return "redirect:/login";
    }
}