package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration class that defines:
 * - URL access permissions based on roles
 * - Login/logout configuration
 * - Password encoding strategy
 * Updated for Spring Security 6.1+ with no deprecated methods
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Configures HTTP security, defining which URLs require authentication
     * and what roles can access specific endpoints
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints - no authentication required
                        .requestMatchers("/", "/register", "/login", "/css/**", "/js/**", "/images/**", "/h2-console/**").permitAll()
                        // Admin endpoints - require ADMIN role
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        // Customer endpoints - require CUSTOMER role
                        .requestMatchers("/customer/**").hasRole("CUSTOMER")
                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler((request, response, authentication) -> {
                            // Custom success handler to avoid redirect loops
                            String redirectUrl = "/dashboard";
                            response.sendRedirect(redirectUrl);
                        })
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers("/h2-console/**") // Disable CSRF for H2 console
                        .disable() // Disabled for development - enable in production
                )
                .headers(headers -> headers
                        .frameOptions(frameOptions -> frameOptions.sameOrigin()) // Allow H2 console frames - Updated syntax
                );

        return http.build();
    }

    /**
     * Password encoder bean for securely hashing passwords
     * BCrypt is a strong hashing algorithm suitable for password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}